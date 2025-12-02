package com.cineclub_backend.cineclub_backend.notifications.repositories;

import com.cineclub_backend.cineclub_backend.notifications.models.Notification;
import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
  Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

  void deleteBySenderIdAndRecipientIdAndType(
    String senderId,
    String recipientId,
    NotificationType type
  );
}
