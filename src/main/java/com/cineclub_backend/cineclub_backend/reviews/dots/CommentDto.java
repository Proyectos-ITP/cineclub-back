package com.cineclub_backend.cineclub_backend.reviews.dots;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class CommentDto {

  private String id;
  private String reviewId;
  private String userId;
  private String userName;
  private String content;
  private List<CommentDto> replies;
  private String parentId;
  private int likes;
  private boolean liked;
  private Date createdAt;
  private Date updatedAt;
}
