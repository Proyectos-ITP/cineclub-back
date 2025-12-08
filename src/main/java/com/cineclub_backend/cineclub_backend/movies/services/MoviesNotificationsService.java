package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.shared.services.WebSocketNotificationService;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto.SenderInfo;
import org.springframework.stereotype.Service;

@Service
public class MoviesNotificationsService {

  private final WebSocketNotificationService notificationService;

  public MoviesNotificationsService(WebSocketNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  public void sendNotification(
    String notificationId,
    String receiverId,
    String senderId,
    NotificationType type,
    String entityId,
    SenderInfo sender
  ) {
    notificationService.sendNewNotification(
      notificationId,
      receiverId,
      type,
      entityId,
      senderId,
      sender
    );
  }
}
