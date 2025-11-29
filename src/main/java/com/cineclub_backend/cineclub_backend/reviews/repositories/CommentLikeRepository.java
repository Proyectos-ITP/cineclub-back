package com.cineclub_backend.cineclub_backend.reviews.repositories;

import com.cineclub_backend.cineclub_backend.reviews.models.CommentLike;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
  CommentLike findByCommentIdAndUserId(String commentId, String userId);
  void deleteAllByCommentId(String commentId);
  void deleteAllByCommentIdIn(java.util.List<String> commentIds);
}
