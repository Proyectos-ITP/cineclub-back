package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.jobs.services.JobQueueService;
import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionRequestResponseDto;
import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import com.cineclub_backend.cineclub_backend.movies.models.CollectionRequest;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRepository;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRequestRepository;
import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.notifications.services.NotificationService;
import com.cineclub_backend.cineclub_backend.shared.services.WebSocketNotificationService;
import com.cineclub_backend.cineclub_backend.shared.templates.CollectionRequestTemplate;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto.SenderInfo;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionRequestService {

  private final CollectionRequestRepository collectionRequestRepository;
  private final CollectionRepository collectionRepository;
  private final UserRepository userRepository;
  private final JobQueueService jobQueueService;
  private final WebSocketNotificationService webSocketNotificationService;
  private final NotificationService persistentNotificationService;
  private final MoviesNotificationsService moviesNotificationsService;
  private final MongoTemplate mongoTemplate;

  public CollectionRequestService(
    CollectionRequestRepository collectionRequestRepository,
    CollectionRepository collectionRepository,
    UserRepository userRepository,
    JobQueueService jobQueueService,
    WebSocketNotificationService webSocketNotificationService,
    NotificationService persistentNotificationService,
    MoviesNotificationsService moviesNotificationsService,
    MongoTemplate mongoTemplate
  ) {
    this.collectionRequestRepository = collectionRequestRepository;
    this.collectionRepository = collectionRepository;
    this.userRepository = userRepository;
    this.jobQueueService = jobQueueService;
    this.webSocketNotificationService = webSocketNotificationService;
    this.persistentNotificationService = persistentNotificationService;
    this.moviesNotificationsService = moviesNotificationsService;
    this.mongoTemplate = mongoTemplate;
  }

  @Transactional
  public void sendCollection(String senderId, String receiverId) {
    if (
      collectionRequestRepository
        .findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "PENDING")
        .isPresent()
    ) {
      throw new RuntimeException("Ya has enviado una solicitud a este usuario.");
    }

    User sender = userRepository
      .findById(senderId)
      .orElseThrow(() -> new RuntimeException("Usuario remitente no encontrado"));
    User receiver = userRepository
      .findById(receiverId)
      .orElseThrow(() -> new RuntimeException("Usuario destinatario no encontrado"));

    CollectionRequest request = new CollectionRequest();
    request.setSenderId(senderId);
    request.setReceiverId(receiverId);
    collectionRequestRepository.save(request);

    String emailBody = CollectionRequestTemplate.collectionShared(
      sender.getFullName(),
      receiver.getFullName(),
      Optional.empty()
    );

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("type", "EMAIL_COLLECTION_REQUEST");
    jobData.put("to", receiver.getEmail());
    jobData.put("subject", "Te han compartido una colección de películas");
    jobData.put("body", emailBody);

    jobQueueService.enqueueJob(jobData);

    CollectionRequestResponseDto notificationDto = toDto(request, sender);
    webSocketNotificationService.sendCollectionRequestNotification(receiverId, notificationDto);

    SenderInfo senderInfo = new SenderInfo();
    senderInfo.setFullName(sender.getFullName());
    senderInfo.setId(sender.getId());

    String notificationId = persistentNotificationService.createNotification(
      receiverId,
      senderId,
      NotificationType.COLLECTION_REQUEST,
      request.getId()
    );

    sendNotification(
      notificationId,
      receiverId,
      senderId,
      NotificationType.COLLECTION_REQUEST,
      request.getId(),
      senderInfo
    );
  }

  private void sendNotification(
    String notificationId,
    String receiverId,
    String senderId,
    NotificationType type,
    String entityId,
    SenderInfo senderInfo
  ) {
    moviesNotificationsService.sendNotification(
      notificationId,
      receiverId,
      senderId,
      type,
      entityId,
      senderInfo
    );
  }

  public List<CollectionRequestResponseDto> getPendingRequests(String userId) {
    List<AggregationOperation> operations = new ArrayList<>();

    FacetOperation facetOperation = Aggregation.facet()
      .and(
        Aggregation.match(Criteria.where("receiver_id").is(userId)),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'users', " +
            "  let: { receiverId: { $toString: '$receiver_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: [ { $toString: '$_id' } , '$$receiverId' ] } } } " +
            "  ], " +
            "  as: 'receiver' " +
            "} }"
        ),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'users', " +
            "  let: { senderId: { $toString: '$sender_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: [ { $toString: '$_id' } , '$$senderId' ] } } } " +
            "  ], " +
            "  as: 'sender' " +
            "} }"
        ),
        Aggregation.unwind("receiver", true),
        Aggregation.unwind("sender", true),
        Aggregation.project("receiver", "sender", "status", "sender_id", "receiver_id", "_id")
          .and(ConditionalOperators.ifNull("created_at").thenValueOf("createdAt"))
          .as("createdAt"),
        Aggregation.match(Criteria.where("status").is("PENDING"))
      )
      .as("data");

    operations.add(facetOperation);
    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
      aggregation,
      "collection_requests",
      Document.class
    );
    Document result = aggregationResults.getUniqueMappedResult();

    if (result == null || !result.containsKey("data")) {
      return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Document> data = (List<Document>) result.get("data");
    return data.stream().map(this::toDto).collect(Collectors.toList());
  }

  public List<CollectionRequestResponseDto> getSendedRequests(String userId) {
    List<AggregationOperation> operations = new ArrayList<>();

    FacetOperation facetOperation = Aggregation.facet()
      .and(
        Aggregation.match(Criteria.where("sender_id").is(userId)),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'users', " +
            "  let: { receiverId: { $toString: '$receiver_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: [ { $toString: '$_id' } , '$$receiverId' ] } } } " +
            "  ], " +
            "  as: 'receiver' " +
            "} }"
        ),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'users', " +
            "  let: { senderId: { $toString: '$sender_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: [ { $toString: '$_id' } , '$$senderId' ] } } } " +
            "  ], " +
            "  as: 'sender' " +
            "} }"
        ),
        Aggregation.unwind("receiver", true),
        Aggregation.unwind("sender", true),
        Aggregation.project("receiver", "sender", "status", "sender_id", "receiver_id", "_id")
          .and(ConditionalOperators.ifNull("created_at").thenValueOf("createdAt"))
          .as("createdAt"),
        Aggregation.match(Criteria.where("status").is("PENDING"))
      )
      .as("data");

    operations.add(facetOperation);
    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
      aggregation,
      "collection_requests",
      Document.class
    );
    Document result = aggregationResults.getUniqueMappedResult();

    if (result == null || !result.containsKey("data")) {
      return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    List<Document> data = (List<Document>) result.get("data");
    return data.stream().map(this::toDto).collect(Collectors.toList());
  }

  private CollectionRequestResponseDto toDto(Document document) {
    CollectionRequestResponseDto dto = new CollectionRequestResponseDto();
    dto.setId(document.getObjectId("_id").toString());
    dto.setSenderId(document.getString("sender_id"));
    dto.setReceiverId(document.getString("receiver_id"));
    dto.setStatus(document.getString("status"));
    if (document.getDate("createdAt") != null) {
      dto.setCreatedAt(
        document.getDate("createdAt").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
      );
    }

    Document sender = document.get("sender", Document.class);
    if (sender != null) {
      dto.setSenderName(sender.getString("fullName"));
      dto.setSenderEmail(sender.getString("email"));
      dto.setSenderId(sender.getString("_id"));
    }

    Document receiver = document.get("receiver", Document.class);
    if (receiver != null) {
      dto.setReceiverName(receiver.getString("fullName"));
      dto.setReceiverEmail(receiver.getString("email"));
      dto.setReceiverId(receiver.getString("_id"));
    }
    return dto;
  }

  @Transactional
  public void acceptRequest(String requestId, String userId) {
    CollectionRequest request = collectionRequestRepository
      .findById(requestId)
      .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

    User receiver = userRepository.findById(request.getReceiverId()).orElse(null);

    if (!request.getReceiverId().equals(userId)) {
      throw new RuntimeException("No tienes permiso para aceptar esta solicitud");
    }

    if (!"PENDING".equals(request.getStatus())) {
      throw new RuntimeException("Esta solicitud ya ha sido procesada");
    }

    Collection senderCollection = collectionRepository
      .findByUserId(request.getSenderId())
      .orElseThrow(() -> new RuntimeException("El remitente no tiene colección"));

    Collection receiverCollection = collectionRepository
      .findByUserId(userId)
      .orElse(new Collection());

    if (receiverCollection.getUserId() == null) {
      receiverCollection.setUserId(userId);
      receiverCollection.setMovies(new ArrayList<>());
    }

    List<String> receiverMovies = receiverCollection.getMovies();
    if (receiverMovies == null) {
      receiverMovies = new ArrayList<>();
    }

    if (senderCollection.getMovies() != null) {
      for (String movieId : senderCollection.getMovies()) {
        if (!receiverMovies.contains(movieId)) {
          receiverMovies.add(movieId);
        }
      }
    }

    receiverCollection.setMovies(receiverMovies);
    collectionRepository.save(receiverCollection);

    request.setStatus("ACCEPTED");
    collectionRequestRepository.save(request);

    SenderInfo senderInfo = new SenderInfo();
    senderInfo.setFullName(receiver.getFullName());
    senderInfo.setId(receiver.getId());

    String notificationId = persistentNotificationService.createNotification(
      request.getSenderId(),
      userId,
      NotificationType.COLLECTION_ACCEPTED,
      request.getId()
    );

    sendNotification(
      notificationId,
      request.getSenderId(),
      userId,
      NotificationType.COLLECTION_ACCEPTED,
      request.getId(),
      senderInfo
    );

    removeCollectionRequestNotification(request.getSenderId(), userId);
  }

  private void removeCollectionRequestNotification(String senderId, String receiverId) {
    persistentNotificationService.removeNotification(
      senderId,
      receiverId,
      NotificationType.COLLECTION_REQUEST
    );
  }

  @Transactional
  public void rejectRequest(String requestId, String userId) {
    CollectionRequest request = collectionRequestRepository
      .findById(requestId)
      .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

    if (!request.getReceiverId().equals(userId)) {
      throw new RuntimeException("No tienes permiso para rechazar esta solicitud");
    }

    collectionRequestRepository.delete(request);
    removeCollectionRequestNotification(request.getSenderId(), userId);
  }

  private CollectionRequestResponseDto toDto(CollectionRequest request, User sender) {
    CollectionRequestResponseDto dto = new CollectionRequestResponseDto();
    dto.setId(request.getId());
    dto.setSenderId(request.getSenderId());
    dto.setStatus(request.getStatus());
    dto.setCreatedAt(request.getCreatedAt());
    if (sender != null) {
      dto.setSenderName(sender.getFullName());
      dto.setSenderEmail(sender.getEmail());
    }
    return dto;
  }
}
