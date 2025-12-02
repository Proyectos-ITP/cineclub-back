package com.cineclub_backend.cineclub_backend.notifications.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  private String id;

  @Field("recipient_id")
  private String recipientId;

  @Field("sender_id")
  private String senderId;

  @Field("type")
  private NotificationType type;

  @Field("entity_id")
  private String entityId;

  @Builder.Default
  @Field("is_read")
  private boolean isRead = false;

  @Field("created_at")
  private LocalDateTime createdAt;
}
