package com.cineclub_backend.cineclub_backend.reviews.models;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comment_likes")
@Data
public class CommentLike {

  @Id
  private String id;

  @Field("comment_id")
  private String commentId;

  @Field("user_id")
  private String userId;

  @Field("created_at")
  private Date createdAt;

  @Field("updated_at")
  private Date updatedAt;

  public CommentLike() {
    this.createdAt = new Date();
    this.updatedAt = new Date();
  }
}
