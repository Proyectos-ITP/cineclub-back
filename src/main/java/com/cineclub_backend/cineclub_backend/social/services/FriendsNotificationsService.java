package com.cineclub_backend.cineclub_backend.social.services;

import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.shared.services.WebSocketNotificationService;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendRequestNotificationDto.SenderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendsNotificationsService {

  private final WebSocketNotificationService notificationService;

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
