package com.cineclub_backend.cineclub_backend.movies.models;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "collection_requests")
@Data
public class CollectionRequest {

  @Id
  private String id;

  @Field("sender_id")
  private String senderId;

  @Field("receiver_id")
  private String receiverId;

  private String status;

  @Field("created_at")
  private LocalDateTime createdAt;

  public CollectionRequest() {
    this.createdAt = LocalDateTime.now();
    this.status = "PENDING";
  }
}
