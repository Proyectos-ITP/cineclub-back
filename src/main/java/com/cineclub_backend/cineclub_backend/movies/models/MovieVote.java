package com.cineclub_backend.cineclub_backend.movies.models;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "movie_votes")
@Data
public class MovieVote {

  @Id
  private String id;

  @Field("user_id")
  private String userId;

  @Field("movie_id")
  private String movieId;

  @Field("type")
  private VoteType type;

  @Field("created_at")
  private LocalDateTime createdAt;

  @Field("updated_at")
  private LocalDateTime updatedAt;

  public enum VoteType {
    UP,
    DOWN,
  }

  public MovieVote() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
}
