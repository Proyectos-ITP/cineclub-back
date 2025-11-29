package com.cineclub_backend.cineclub_backend.reviews.dots;

import java.util.Date;
import lombok.Data;

@Data
public class ReviewDto {

  private String id;
  private String title;
  private String content;
  private Integer rating;
  private String reviewerName;
  private String directorName;
  private String posterPath;
  private String movieId;
  private String userId;
  private boolean liked;
  private Integer likes;
  private Integer comments;
  private Date createdAt;
  private Date updatedAt;
}
