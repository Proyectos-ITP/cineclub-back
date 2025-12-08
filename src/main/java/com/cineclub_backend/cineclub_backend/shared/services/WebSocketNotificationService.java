package com.cineclub_backend.cineclub_backend.shared.services;

import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto.SenderInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Envía una notificación a un usuario específico
   *
   * @param userId ID del usuario destinatario
   * @param destination Destino del mensaje (ej: "/queue/friend-requests")
   * @param payload Contenido de la notificación
   */
  public void sendNotificationToUser(String userId, String destination, Object payload) {
    try {
      String userDestination = "/user/" + userId + destination;
      messagingTemplate.convertAndSend(userDestination, payload);
      log.info("Notification sent to user {} at destination {}", userId, userDestination);
    } catch (Exception e) {
      log.error("Error sending notification to user {}: {}", userId, e.getMessage());
    }
  }

  /**
   * Envía una notificación de solicitud de amistad
   *
   * @param userId ID del usuario destinatario
   * @param notification DTO con la información de la solicitud
   */
  public void sendFriendRequestNotification(String userId, Object notification) {
    sendNotificationToUser(userId, "/queue/friend-requests", notification);
  }

  /**
   * Envía una notificación de solicitud de compartir colección
   *
   * @param userId ID del usuario destinatario
   * @param notification DTO con la información de la solicitud
   */
  public void sendCollectionRequestNotification(String userId, Object notification) {
    sendNotificationToUser(userId, "/queue/collection-requests", notification);
  }

  /**
   * Envía una notificación a un usuario específico
   *
   * @param userId ID del usuario destinatario
   * @param destination Destino del mensaje (ej: "/queue/friend-requests")
   * @param payload Contenido de la notificación
   */
  public void sendNewNotification(
    String notificationId,
    String userId,
    NotificationType notificationType,
    String entityId,
    String senderId,
    SenderInfo sender
  ) {
    Map<String, Object> notification = new HashMap<>();
    notification.put("id", notificationId);
    notification.put("type", notificationType);
    notification.put("entityId", entityId);
    notification.put("senderId", senderId);
    notification.put("receiverId", userId);
    notification.put("sender", sender);
    sendNotificationToUser(userId, "/notifications", notification);
  }
}
