package com.cineclub_backend.cineclub_backend.reviews.services;

import com.cineclub_backend.cineclub_backend.reviews.models.Review;
import com.cineclub_backend.cineclub_backend.reviews.models.ReviewLike;
import com.cineclub_backend.cineclub_backend.reviews.repositories.ReviewLikeRepository;
import java.util.NoSuchElementException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CrudReviewLikeService {

  private final ReviewLikeRepository reviewLikeRepository;
  private final CrudReviewService crudReviewService;

  public CrudReviewLikeService(
    ReviewLikeRepository reviewLikeRepository,
    @Lazy CrudReviewService crudReviewService
  ) {
    this.reviewLikeRepository = reviewLikeRepository;
    this.crudReviewService = crudReviewService;
  }

  public String createLikeReview(String reviewId, String userId) {
    Review review = crudReviewService.findById(reviewId);

    if (review == null) {
      throw new NoSuchElementException("Reseña no encontrada");
    }

    ReviewLike reviewLikeExists = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
    if (reviewLikeExists != null) {
      return reviewLikeExists.getId();
    }
    ReviewLike reviewLike = new ReviewLike();
    reviewLike.setReviewId(reviewId);
    reviewLike.setUserId(userId);
    return reviewLikeRepository.save(reviewLike).getId();
  }

  public void removeAllLikeReview(String reviewId) {
    reviewLikeRepository.deleteAllByReviewId(reviewId);
  }

  public String removeLikeReview(String reviewId, String userId) {
    ReviewLike reviewLikeExists = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
    if (reviewLikeExists == null) {
      throw new NoSuchElementException("No tienes permiso para eliminar este like");
    }
    reviewLikeRepository.delete(reviewLikeExists);
    return reviewLikeExists.getId();
  }
}
