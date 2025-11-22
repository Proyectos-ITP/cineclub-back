package com.cineclub_backend.cineclub_backend.reviews.models;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "reviews")
@Data
public class Review {

  @Id
  private String id;

  @Field("content")
  private String content;

  @Field("user_id")
  private String userId;

  @Field("movie_id")
  private String movieId;

  @Field("rating")
  private Integer rating;

  @Field("created_at")
  private Date createdAt;

  @Field("updated_at")
  private Date updatedAt;

  public Review() {
    this.createdAt = new Date();
    this.updatedAt = new Date();
  }

  public void update(Review review) {
    this.content = review.getContent();
    this.rating = review.getRating();
    this.updatedAt = new Date();
  }
}
