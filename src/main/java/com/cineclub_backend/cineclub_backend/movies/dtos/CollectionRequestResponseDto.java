package com.cineclub_backend.cineclub_backend.movies.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CollectionRequestResponseDto {

  private String id;
  private String senderId;
  private String senderName;
  private String senderEmail;
  private String receiverId;
  private String receiverName;
  private String receiverEmail;
  private String status;
  private LocalDateTime createdAt;
}
