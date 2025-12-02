package com.cineclub_backend.cineclub_backend.notifications.services;

import com.cineclub_backend.cineclub_backend.notifications.models.Notification;
import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.notifications.repositories.NotificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public void createNotification(
    String recipientId,
    String senderId,
    NotificationType type,
    String entityId
  ) {
    Notification notification = Notification.builder()
      .recipientId(recipientId)
      .senderId(senderId)
      .type(type)
      .entityId(entityId)
      .createdAt(LocalDateTime.now())
      .isRead(false)
      .build();

    notificationRepository.save(notification);
  }

  public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
    return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
  }

  public void markAsRead(String notificationId) {
    notificationRepository
      .findById(notificationId)
      .ifPresent(notification -> {
        notification.setRead(true);
        notificationRepository.save(notification);
      });
  }
}
