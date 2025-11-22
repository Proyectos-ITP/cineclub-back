package com.cineclub_backend.cineclub_backend.movies.repositories;

import com.cineclub_backend.cineclub_backend.movies.models.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
  Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
