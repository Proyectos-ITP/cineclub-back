package com.cineclub_backend.cineclub_backend.movies.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cineclub_backend.cineclub_backend.movies.models.Movie;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
