package com.cineclub_backend.cineclub_backend.movies.repositories;

import com.cineclub_backend.cineclub_backend.movies.models.Director;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectorsRepository extends MongoRepository<Director, String> {
  Page<Director> findByDirectorContainingIgnoreCase(String director, Pageable pageable);
  Optional<Director> findByMovieId(String movieId);
}
