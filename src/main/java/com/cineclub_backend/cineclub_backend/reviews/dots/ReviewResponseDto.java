package com.cineclub_backend.cineclub_backend.reviews.dots;

import java.util.List;
import lombok.Data;

@Data
public class ReviewResponseDto {

  private String id;
  private String title;
  private String content;
  private String reviewerName;
  private String directorName;
  private List<String> genres;
  private String rating;
  private String createdAt;
  private String updatedAt;
}
