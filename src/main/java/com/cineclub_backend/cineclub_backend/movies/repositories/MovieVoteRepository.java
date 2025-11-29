package com.cineclub_backend.cineclub_backend.movies.repositories;

import com.cineclub_backend.cineclub_backend.movies.models.MovieVote;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovieVoteRepository extends MongoRepository<MovieVote, String> {
  Optional<MovieVote> findByUserIdAndMovieId(String userId, String movieId);
}
