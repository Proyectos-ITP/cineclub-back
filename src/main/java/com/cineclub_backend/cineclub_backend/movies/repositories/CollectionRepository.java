package com.cineclub_backend.cineclub_backend.movies.repositories;

import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends MongoRepository<Collection, String> {
  Optional<Collection> findByUserId(String userId);
}
