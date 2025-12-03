package com.cineclub_backend.cineclub_backend.users.services;

import com.cineclub_backend.cineclub_backend.social.repositories.Neo4jClient;
import com.cineclub_backend.cineclub_backend.users.dtos.UserDto;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class CrudUserService {

  private final UserRepository userRepository;
  private final MongoTemplate mongoTemplate;
  private final Neo4jClient neo4jClient;

  public CrudUserService(
    UserRepository userRepository,
    MongoTemplate mongoTemplate,
    Neo4jClient neo4jClient
  ) {
    this.userRepository = userRepository;
    this.mongoTemplate = mongoTemplate;
    this.neo4jClient = neo4jClient;
  }

  public User getUserById(String id) {
    return userRepository.findById(id).orElse(null);
  }

  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User saveUser(User user) {
    User savedUser = userRepository.save(user);
    try {
      neo4jClient.upsertUser(savedUser);
    } catch (Exception e) {
      System.err.println("Error syncing user to Neo4j: " + e.getMessage());
    }
    return savedUser;
  }

  public void deleteUser(String id) {
    userRepository.deleteById(id);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public Page<User> getUsersPaginated(String name, String email, Pageable pageable) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();
      List<Criteria> criteriaList = new ArrayList<>();

      if (name != null && !name.trim().isEmpty()) {
        criteriaList.add(Criteria.where("fullName").regex(name, "i"));
      }
      if (email != null && !email.trim().isEmpty()) {
        criteriaList.add(Criteria.where("email").regex(email, "i"));
      }

      if (!criteriaList.isEmpty()) {
        Criteria combinedCriteria = new Criteria().andOperator(
          criteriaList.toArray(new Criteria[0])
        );
        operations.add(Aggregation.match(combinedCriteria));
      }

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize())
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);
      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "users",
        Document.class
      );

      Document result = results.getUniqueMappedResult();

      if (result == null) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
      }

      long total = 0;
      Object metadataObj = result.get("metadata");
      if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
        Object firstItem = metadataList.get(0);
        if (firstItem instanceof Document metadataDoc) {
          total = metadataDoc.getInteger("total", 0);
        }
      }

      List<User> users = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            users.add(mongoTemplate.getConverter().read(User.class, doc));
          }
        }
      }

      return new PageImpl<>(users, pageable, total);
    } catch (Exception e) {
      throw new RuntimeException("Error al obtener usuarios paginados: " + e.getMessage(), e);
    }
  }

  public Page<UserDto> getNotFriendsPaginated(
    String userId,
    String name,
    String email,
    Pageable pageable
  ) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      operations.add(Aggregation.match(Criteria.where("_id").ne(userId)));
      operations.add(Aggregation.lookup("friends", "_id", "friend_id", "friendships_as_friend"));
      operations.add(Aggregation.lookup("friends", "_id", "user_id", "friendships_as_user"));
      operations.add(context ->
        new Document(
          "$addFields",
          new Document(
            "all_friendships",
            new Document("$concatArrays", List.of("$friendships_as_friend", "$friendships_as_user"))
          )
        )
      );

      Document filterCondition = new Document();
      filterCondition.append("input", "$all_friendships");
      filterCondition.append("as", "friendship");
      filterCondition.append(
        "cond",
        new Document(
          "$or",
          List.of(
            new Document("$eq", List.of("$$friendship.user_id", userId)),
            new Document("$eq", List.of("$$friendship.friend_id", userId))
          )
        )
      );

      operations.add(context ->
        new Document(
          "$addFields",
          new Document("is_friend", new Document("$filter", filterCondition))
        )
      );

      operations.add(Aggregation.match(Criteria.where("is_friend").size(0)));

      if (name != null && !name.trim().isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("fullName").regex(name, "i")));
      }

      if (email != null && !email.trim().isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("email").regex(email, "i")));
      }

      operations.add(Aggregation.lookup("friendRequests", "_id", "receiver_id", "requests_sent"));
      operations.add(Aggregation.lookup("friendRequests", "_id", "sender_id", "requests_received"));

      operations.add(context ->
        new Document(
          "$addFields",
          new Document(
            "pending_request_sent",
            new Document(
              "$arrayElemAt",
              List.of(
                new Document(
                  "$filter",
                  new Document()
                    .append("input", "$requests_sent")
                    .append("as", "req")
                    .append(
                      "cond",
                      new Document(
                        "$and",
                        List.of(
                          new Document("$eq", List.of("$$req.sender_id", userId)),
                          new Document("$eq", List.of("$$req.status", "PENDING"))
                        )
                      )
                    )
                ),
                0
              )
            )
          ).append(
            "pending_request_received",
            new Document(
              "$arrayElemAt",
              List.of(
                new Document(
                  "$filter",
                  new Document()
                    .append("input", "$requests_received")
                    .append("as", "req")
                    .append(
                      "cond",
                      new Document(
                        "$and",
                        List.of(
                          new Document("$eq", List.of("$$req.receiver_id", userId)),
                          new Document("$eq", List.of("$$req.status", "PENDING"))
                        )
                      )
                    )
                ),
                0
              )
            )
          )
        )
      );

      operations.add(
        Aggregation.project()
          .and("_id")
          .as("id")
          .and("fullName")
          .as("fullName")
          .and("email")
          .as("email")
          .and("country")
          .as("country")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$or",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_sent"), "missing")
                    ),
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    )
                  )
                ),
                true,
                false
              )
            )
          )
          .as("hasPendingRequest")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$ne",
                  List.of(new Document("$type", "$pending_request_sent"), "missing")
                ),
                new Document("$toString", "$pending_request_sent._id"),
                new Document(
                  "$cond",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    ),
                    new Document("$toString", "$pending_request_received._id"),
                    "$$REMOVE"
                  )
                )
              )
            )
          )
          .as("pendingRequestId")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$ne",
                  List.of(new Document("$type", "$pending_request_sent"), "missing")
                ),
                true,
                new Document(
                  "$cond",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    ),
                    false,
                    "$$REMOVE"
                  )
                )
              )
            )
          )
          .as("isSender")
      );

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize())
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);
      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "users",
        Document.class
      );

      Document result = results.getUniqueMappedResult();

      if (result == null) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
      }

      long total = 0;
      Object metadataObj = result.get("metadata");
      if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
        Object firstItem = metadataList.get(0);
        if (firstItem instanceof Document metadataDoc) {
          total = metadataDoc.getInteger("total", 0);
        }
      }

      List<UserDto> users = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            users.add(convertDocumentToUserDto(doc));
          }
        }
      }

      return new PageImpl<>(users, pageable, total);
    } catch (Exception e) {
      throw new RuntimeException(
        "Error al obtener usuarios que no son amigos: " + e.getMessage(),
        e
      );
    }
  }

  public List<UserDto> getUsersDetails(String currentUserId, List<String> userIds) {
    try {
      if (userIds.isEmpty()) {
        return new ArrayList<>();
      }

      List<AggregationOperation> operations = new ArrayList<>();

      operations.add(Aggregation.match(Criteria.where("_id").in(userIds)));

      operations.add(Aggregation.lookup("friendRequests", "_id", "receiver_id", "requests_sent"));
      operations.add(Aggregation.lookup("friendRequests", "_id", "sender_id", "requests_received"));

      operations.add(context ->
        new Document(
          "$addFields",
          new Document(
            "pending_request_sent",
            new Document(
              "$arrayElemAt",
              List.of(
                new Document(
                  "$filter",
                  new Document()
                    .append("input", "$requests_sent")
                    .append("as", "req")
                    .append(
                      "cond",
                      new Document(
                        "$and",
                        List.of(
                          new Document("$eq", List.of("$$req.sender_id", currentUserId)),
                          new Document("$eq", List.of("$$req.status", "PENDING"))
                        )
                      )
                    )
                ),
                0
              )
            )
          ).append(
            "pending_request_received",
            new Document(
              "$arrayElemAt",
              List.of(
                new Document(
                  "$filter",
                  new Document()
                    .append("input", "$requests_received")
                    .append("as", "req")
                    .append(
                      "cond",
                      new Document(
                        "$and",
                        List.of(
                          new Document("$eq", List.of("$$req.receiver_id", currentUserId)),
                          new Document("$eq", List.of("$$req.status", "PENDING"))
                        )
                      )
                    )
                ),
                0
              )
            )
          )
        )
      );

      operations.add(
        Aggregation.project()
          .and("_id")
          .as("id")
          .and("fullName")
          .as("fullName")
          .and("email")
          .as("email")
          .and("country")
          .as("country")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$or",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_sent"), "missing")
                    ),
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    )
                  )
                ),
                true,
                false
              )
            )
          )
          .as("hasPendingRequest")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$ne",
                  List.of(new Document("$type", "$pending_request_sent"), "missing")
                ),
                new Document("$toString", "$pending_request_sent._id"),
                new Document(
                  "$cond",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    ),
                    new Document("$toString", "$pending_request_received._id"),
                    "$$REMOVE"
                  )
                )
              )
            )
          )
          .as("pendingRequestId")
          .and(context ->
            new Document(
              "$cond",
              List.of(
                new Document(
                  "$ne",
                  List.of(new Document("$type", "$pending_request_sent"), "missing")
                ),
                true,
                new Document(
                  "$cond",
                  List.of(
                    new Document(
                      "$ne",
                      List.of(new Document("$type", "$pending_request_received"), "missing")
                    ),
                    false,
                    "$$REMOVE"
                  )
                )
              )
            )
          )
          .as("isSender")
      );

      Aggregation aggregation = Aggregation.newAggregation(operations);
      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "users",
        Document.class
      );

      List<UserDto> users = new ArrayList<>();
      for (Document doc : results.getMappedResults()) {
        users.add(convertDocumentToUserDto(doc));
      }

      return users;
    } catch (Exception e) {
      throw new RuntimeException("Error al obtener detalles de usuarios: " + e.getMessage(), e);
    }
  }

  private UserDto convertDocumentToUserDto(Document doc) {
    UserDto userDto = new UserDto();
    userDto.setId(doc.getString("id"));
    userDto.setFullName(doc.getString("fullName"));
    userDto.setEmail(doc.getString("email"));
    userDto.setCountry(doc.getString("country"));
    userDto.setHasPendingRequest(doc.getBoolean("hasPendingRequest", false));

    Object pendingRequestId = doc.get("pendingRequestId");
    if (pendingRequestId != null) {
      userDto.setPendingRequestId(pendingRequestId.toString());
    }

    userDto.setIsSender(doc.getBoolean("isSender"));
    return userDto;
  }
}
