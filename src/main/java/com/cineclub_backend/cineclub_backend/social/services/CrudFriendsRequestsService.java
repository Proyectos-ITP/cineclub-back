package com.cineclub_backend.cineclub_backend.social.services;

import com.cineclub_backend.cineclub_backend.jobs.services.JobQueueService;
import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.notifications.services.NotificationService;
import com.cineclub_backend.cineclub_backend.shared.services.WebSocketNotificationService;
import com.cineclub_backend.cineclub_backend.shared.templates.FriendsRequestTemplate;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto.SenderInfo;
import com.cineclub_backend.cineclub_backend.social.models.Friend;
import com.cineclub_backend.cineclub_backend.social.models.FriendRequest;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendRequestRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendsRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.Neo4jClient;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrudFriendsRequestsService {

  private final FriendRequestRepository friendRequestRepository;
  private final FriendsRepository friendsRepository;
  private final UserRepository userRepository;
  private final WebSocketNotificationService notificationService;
  private final JobQueueService jobQueueService;
  private final NotificationService persistentNotificationService;
  private final FriendsNotificationsService friendsNotificationsService;
  private final Neo4jClient neo4jClient;

  public FriendRequest sendFriendRequest(String userId, String receiverId) {
    Optional<FriendRequest> existingRequest = friendRequestRepository.findBySenderIdAndReceiverId(
      userId,
      receiverId
    );

    if (existingRequest.isPresent()) {
      throw new RuntimeException("Ya existe una solicitud de amistad");
    }

    FriendRequest friendRequest = new FriendRequest();
    friendRequest.setSenderId(userId);
    friendRequest.setReceiverId(receiverId);
    friendRequest.setCreatedAt(new Date());
    friendRequest.setStatus("PENDING");

    FriendRequest savedRequest = friendRequestRepository.save(friendRequest);

    sendFriendRequestEmailNotification(savedRequest);

    String notificationId = persistentNotificationService.createNotification(
      receiverId,
      userId,
      NotificationType.FRIEND_REQUEST,
      savedRequest.getId()
    );

    sendFriendRequestNotification(savedRequest, notificationId);
    return savedRequest;
  }

  private void sendFriendRequestEmailNotification(FriendRequest friendRequest) {
    User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
    User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

    if (sender != null && receiver != null) {
      Map<String, Object> job = new HashMap<>();
      job.put("type", "EMAIL_FRIEND_REQUEST");
      job.put("to", receiver.getEmail());
      job.put("subject", "Solicitud de amistad");
      job.put(
        "body",
        FriendsRequestTemplate.friendRequestSent(
          sender.getFullName(),
          receiver.getFullName(),
          Optional.empty()
        )
      );

      jobQueueService.enqueueJob(job);
    }
  }

  private void sendFriendRequestNotification(FriendRequest friendRequest, String notificationId) {
    User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);
    User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);

    SenderInfo senderInfo = new SenderInfo();
    senderInfo.setFullName(sender.getFullName());
    senderInfo.setId(sender.getId());

    if (receiver != null) {
      FriendRequestNotificationDto notification = FriendRequestNotificationDto.builder()
        .id(friendRequest.getId())
        .senderId(friendRequest.getSenderId())
        .receiverId(friendRequest.getReceiverId())
        .sender(senderInfo)
        .createdAt(friendRequest.getCreatedAt())
        .status(friendRequest.getStatus())
        .build();

      notificationService.sendFriendRequestNotification(
        friendRequest.getReceiverId(),
        notification
      );

      friendsNotificationsService.sendNotification(
        notificationId,
        friendRequest.getReceiverId(),
        friendRequest.getSenderId(),
        NotificationType.FRIEND_REQUEST,
        friendRequest.getId(),
        senderInfo
      );
    }
  }

  @Transactional
  public void acceptFriendRequest(String userId, String senderId) {
    FriendRequest friendRequest = friendRequestRepository
      .findBySenderIdAndReceiverId(senderId, userId)
      .orElseThrow(() -> new RuntimeException("No se encontró la solicitud de amistad"));

    if (!"PENDING".equals(friendRequest.getStatus())) {
      throw new RuntimeException("La solicitud ya fue procesada");
    }

    Date now = new Date();

    Friend friendship1 = new Friend();
    friendship1.setUserId(senderId);
    friendship1.setFriendId(userId);
    friendship1.setCreatedAt(now);

    Friend friendship2 = new Friend();
    friendship2.setUserId(userId);
    friendship2.setFriendId(senderId);
    friendship2.setCreatedAt(now);

    friendsRepository.save(friendship1);
    friendsRepository.save(friendship2);

    try {
      neo4jClient.addFriendship(senderId, userId);
    } catch (Exception e) {
      System.err.println("Error syncing friendship to Neo4j: " + e.getMessage());
    }

    friendRequest.setStatus("ACCEPTED");
    FriendRequest updatedRequest = friendRequestRepository.save(friendRequest);

    sendFriendRequestAcceptedEmailNotification(updatedRequest);

    String notificationId = persistentNotificationService.createNotification(
      senderId,
      userId,
      NotificationType.FRIEND_ACCEPTED,
      updatedRequest.getId()
    );

    sendFriendRequestAcceptedNotification(updatedRequest, userId, notificationId);
    removeFriendRequestNotification(senderId, userId);
  }

  private void removeFriendRequestNotification(String senderId, String receiverId) {
    persistentNotificationService.removeNotification(
      senderId,
      receiverId,
      NotificationType.FRIEND_REQUEST
    );
  }

  private void sendFriendRequestAcceptedEmailNotification(FriendRequest friendRequest) {
    User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
    User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

    if (sender != null && receiver != null) {
      Map<String, Object> job = new HashMap<>();
      job.put("type", "EMAIL_FRIEND_ACCEPTED");
      job.put("to", sender.getEmail());
      job.put("subject", "Solicitud de amistad aceptada");
      job.put(
        "body",
        FriendsRequestTemplate.friendRequestAccepted(
          sender.getFullName(),
          receiver.getFullName(),
          Optional.empty()
        )
      );

      jobQueueService.enqueueJob(job);
    }
  }

  /**
   * Construye y envía una notificación de solicitud aceptada al remitente original
   */
  private void sendFriendRequestAcceptedNotification(
    FriendRequest friendRequest,
    String acceptedById,
    String notificationId
  ) {
    User acceptedByUser = userRepository.findById(acceptedById).orElse(null);

    SenderInfo senderInfo = new SenderInfo();
    senderInfo.setFullName(acceptedByUser.getFullName());
    senderInfo.setId(acceptedByUser.getId());

    if (acceptedByUser != null) {
      friendsNotificationsService.sendNotification(
        notificationId,
        friendRequest.getSenderId(),
        friendRequest.getReceiverId(),
        NotificationType.FRIEND_ACCEPTED,
        friendRequest.getId(),
        senderInfo
      );
    }
  }

  public void rejectFriendRequest(String userId, String senderId) {
    FriendRequest friendRequest = friendRequestRepository
      .findBySenderIdAndReceiverId(senderId, userId)
      .orElseThrow(() -> new RuntimeException("No se encontró la solicitud de amistad"));

    if (!"PENDING".equals(friendRequest.getStatus())) {
      throw new RuntimeException("La solicitud ya fue procesada");
    }

    friendRequestRepository.delete(friendRequest);

    sendFriendRequestRejectedEmailNotification(friendRequest);
    removeFriendRequestNotification(senderId, userId);
  }

  private void sendFriendRequestRejectedEmailNotification(FriendRequest friendRequest) {
    User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
    User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

    if (sender != null && receiver != null) {
      Map<String, Object> job = new HashMap<>();
      job.put("type", "EMAIL_FRIEND_REJECTED");
      job.put("to", sender.getEmail());
      job.put("subject", "Solicitud de amistad rechazada");
      job.put(
        "body",
        FriendsRequestTemplate.friendRequestRejected(
          sender.getFullName(),
          receiver.getFullName(),
          Optional.empty()
        )
      );

      jobQueueService.enqueueJob(job);
    }
  }

  public List<FriendRequest> getReceivedRequests(String userId) {
    List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverId(userId);
    return receivedRequests
      .stream()
      .filter(req -> "PENDING".equals(req.getStatus()))
      .collect(Collectors.toList());
  }

  public List<FriendRequest> getSentRequests(String userId) {
    List<FriendRequest> sentRequests = friendRequestRepository.findBySenderId(userId);

    return sentRequests
      .stream()
      .filter(req -> "PENDING".equals(req.getStatus()))
      .collect(Collectors.toList());
  }

  public void cancelFriendRequest(String userId, String receiverId) {
    FriendRequest friendRequest = friendRequestRepository
      .findBySenderIdAndReceiverId(userId, receiverId)
      .orElseThrow(() -> new RuntimeException("No se encontró la solicitud de amistad"));

    if (!"PENDING".equals(friendRequest.getStatus())) {
      throw new RuntimeException("La solicitud ya fue procesada");
    }

    friendRequestRepository.delete(friendRequest);
    removeFriendRequestNotification(userId, receiverId);
  }
}
