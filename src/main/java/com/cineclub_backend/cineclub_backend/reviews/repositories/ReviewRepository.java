package com.cineclub_backend.cineclub_backend.reviews.repositories;

import com.cineclub_backend.cineclub_backend.reviews.models.Review;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
  Optional<Review> findByUserId(String userId);
}
