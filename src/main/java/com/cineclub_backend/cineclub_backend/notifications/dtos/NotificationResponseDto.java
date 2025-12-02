package com.cineclub_backend.cineclub_backend.notifications.dtos;

import com.cineclub_backend.cineclub_backend.notifications.models.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

  private String id;
  private String senderId;
  private String recipientId;
  private SenderInfo sender;
  private NotificationType type;
  private LocalDateTime createdAt;
  private String entityId;
  private boolean isRead;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SenderInfo {

    private String id;
    private String fullName;
    private String email;
  }
}
