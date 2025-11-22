package com.cineclub_backend.cineclub_backend.social.dtos;

import java.util.Date;
import lombok.Data;

@Data
public class FriendResponseDto {

  private String id;
  private String userId;
  private FriendInfo friend;
  private Date createdAt;

  @Data
  public static class FriendInfo {

    private String id;
    private String fullName;
    private String email;
  }
}
