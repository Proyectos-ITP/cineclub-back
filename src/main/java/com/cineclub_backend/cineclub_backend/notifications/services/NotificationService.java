package com.cineclub_backend.cineclub_backend.notifications.services;

import com.cineclub_backend.cineclub_backend.notifications.dtos.NotificationResponseDto;
import com.cineclub_backend.cineclub_backend.notifications.models.Notification;
import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import com.cineclub_backend.cineclub_backend.notifications.repositories.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final MongoTemplate mongoTemplate;

  public String createNotification(
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
    return notification.getId();
  }

  public Page<NotificationResponseDto> getUserNotifications(String userId, Pageable pageable) {
    Criteria criteria = Criteria.where("recipient_id").is(userId);

    AggregationOperation match = Aggregation.match(criteria);
    AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "created_at");
    AggregationOperation skip = Aggregation.skip(
      (long) pageable.getPageNumber() * pageable.getPageSize()
    );
    AggregationOperation limit = Aggregation.limit(pageable.getPageSize());

    LookupOperation lookup = LookupOperation.newLookup()
      .from("users")
      .localField("sender_id")
      .foreignField("_id")
      .as("sender");

    AggregationOperation unwind = Aggregation.unwind("sender", true);

    AggregationOperation project = Aggregation.project()
      .and("_id")
      .as("id")
      .and("sender_id")
      .as("senderId")
      .and("recipient_id")
      .as("recipientId")
      .and("type")
      .as("type")
      .and("entity_id")
      .as("entityId")
      .and("is_read")
      .as("isRead")
      .and("created_at")
      .as("createdAt")
      .and("sender._id")
      .as("sender.id")
      .and("sender.fullName")
      .as("sender.fullName")
      .and("sender.email")
      .as("sender.email");

    Aggregation aggregation = Aggregation.newAggregation(
      match,
      sort,
      skip,
      limit,
      lookup,
      unwind,
      project
    );

    List<NotificationResponseDto> results = mongoTemplate
      .aggregate(aggregation, "notifications", NotificationResponseDto.class)
      .getMappedResults();

    long total = notificationRepository.count(
      org.springframework.data.domain.Example.of(Notification.builder().recipientId(userId).build())
    );

    return new PageImpl<>(results, pageable, total);
  }

  public void removeNotification(String senderId, String recipientId, NotificationType type) {
    notificationRepository.deleteBySenderIdAndRecipientIdAndType(senderId, recipientId, type);
  }

  public void markAsRead(String notificationId) {
    notificationRepository
      .findById(notificationId)
      .ifPresent(notification -> {
        notification.setRead(true);
        notificationRepository.save(notification);
      });
  }

  public long getCount(String userId) {
    return notificationRepository.count(
      Example.of(Notification.builder().recipientId(userId).build())
    );
  }
}
