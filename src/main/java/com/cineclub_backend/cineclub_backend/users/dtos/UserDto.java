package com.cineclub_backend.cineclub_backend.users.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private String id;
  private String fullName;
  private String email;
  private String country;

  private Boolean hasPendingRequest;
  private String pendingRequestId;
  private Boolean isSender;
}
