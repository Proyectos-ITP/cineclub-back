package com.cineclub_backend.cineclub_backend.social.dtos;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestNotificationDto {

  private String id;
  private String senderId;
  private String receiverId;
  private SenderInfo sender;
  private Date createdAt;
  private String status;

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
