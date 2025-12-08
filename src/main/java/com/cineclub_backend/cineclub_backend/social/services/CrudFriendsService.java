package com.cineclub_backend.cineclub_backend.social.services;

import com.cineclub_backend.cineclub_backend.social.dtos.FriendResponseDto;
import com.cineclub_backend.cineclub_backend.social.models.FriendRequest;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendRequestRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendsRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.Neo4jClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrudFriendsService {

  private final FriendsRepository friendsRepository;
  private final FriendRequestRepository friendRequestRepository;
  private final MongoTemplate mongoTemplate;
  private final Neo4jClient neo4jClient;

  public CrudFriendsService(
    FriendsRepository friendsRepository,
    FriendRequestRepository friendRequestRepository,
    MongoTemplate mongoTemplate,
    Neo4jClient neo4jClient
  ) {
    this.friendsRepository = friendsRepository;
    this.friendRequestRepository = friendRequestRepository;
    this.mongoTemplate = mongoTemplate;
    this.neo4jClient = neo4jClient;
  }

  public Page<FriendResponseDto> getFriendsPaginated(
    String userId,
    String name,
    Pageable pageable
  ) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      operations.add(Aggregation.match(Criteria.where("user_id").is(userId)));
      operations.add(Aggregation.lookup("users", "friend_id", "_id", "friendDetails"));
      operations.add(Aggregation.unwind("friendDetails"));

      if (name != null && !name.trim().isEmpty()) {
        operations.add(
          Aggregation.match(Criteria.where("friendDetails.fullName").regex(name, "i"))
        );
      }

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize()),
          Aggregation.project()
            .and("_id")
            .as("_id")
            .and("user_id")
            .as("user_id")
            .and("friend_id")
            .as("friend_id")
            .and("createdAt")
            .as("createdAt")
            .and("friendDetails._id")
            .as("friendId")
            .and("friendDetails.fullName")
            .as("friendName")
            .and("friendDetails.email")
            .as("friendEmail")
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);
      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "friends",
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

      List<FriendResponseDto> friendDtos = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            friendDtos.add(convertDocumentToFriendResponseDto(doc));
          }
        }
      }

      return new PageImpl<>(friendDtos, pageable, total);
    } catch (Exception e) {
      throw new RuntimeException("Error al obtener amigos paginados: " + e.getMessage(), e);
    }
  }

  private FriendResponseDto convertDocumentToFriendResponseDto(Document doc) {
    FriendResponseDto dto = new FriendResponseDto();
    dto.setId(doc.getObjectId("_id") != null ? doc.getObjectId("_id").toString() : null);
    dto.setUserId(doc.getString("user_id"));
    dto.setCreatedAt(doc.getDate("createdAt"));

    FriendResponseDto.FriendInfo friendInfo = new FriendResponseDto.FriendInfo();
    friendInfo.setId(doc.getString("friendId"));
    friendInfo.setFullName(doc.getString("friendName"));
    friendInfo.setEmail(doc.getString("friendEmail"));

    dto.setFriend(friendInfo);

    return dto;
  }

  @Transactional
  public void deleteFriend(String userId, String friendId) {
    friendsRepository
      .findByUserIdAndFriendId(userId, friendId)
      .orElseThrow(() -> new RuntimeException("No se encontró la amistad"));

    friendsRepository.deleteByUserIdAndFriendId(userId, friendId);
    friendsRepository.deleteByUserIdAndFriendId(friendId, userId);

    Optional<FriendRequest> request1 = friendRequestRepository.findBySenderIdAndReceiverId(
      userId,
      friendId
    );
    Optional<FriendRequest> request2 = friendRequestRepository.findBySenderIdAndReceiverId(
      friendId,
      userId
    );

    if (request1.isPresent()) {
      friendRequestRepository.delete(request1.get());
    }
    if (request2.isPresent()) {
      friendRequestRepository.delete(request2.get());
    }

    try {
      System.out.println("Removing friendship from Neo4j: " + userId + " and " + friendId);
      neo4jClient.removeFriendship(userId, friendId);
    } catch (Exception e) {
      System.err.println("Error removing friendship from Neo4j: " + e.getMessage());
    }
  }
}
