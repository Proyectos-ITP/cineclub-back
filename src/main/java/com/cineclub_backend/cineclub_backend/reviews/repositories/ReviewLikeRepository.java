package com.cineclub_backend.cineclub_backend.reviews.repositories;

import com.cineclub_backend.cineclub_backend.reviews.models.ReviewLike;
import org.springframework.data.repository.CrudRepository;

public interface ReviewLikeRepository extends CrudRepository<ReviewLike, String> {
  ReviewLike findByReviewIdAndUserId(String reviewId, String userId);
  void deleteAllByReviewId(String reviewId);
}
