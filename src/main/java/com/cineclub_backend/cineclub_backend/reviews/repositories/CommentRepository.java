package com.cineclub_backend.cineclub_backend.reviews.repositories;

import com.cineclub_backend.cineclub_backend.reviews.models.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
  Page<Comment> findByReviewId(String reviewId, Pageable pageable);
  void deleteByParentId(String parentId);
  void deleteAllByReviewId(String reviewId);
  List<Comment> findByParentId(String parentId);
  List<Comment> findAllByReviewId(String reviewId);
  void deleteAllByParentIdIn(List<String> parentIds);
}
