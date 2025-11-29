package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.movies.models.Movie;
import com.cineclub_backend.cineclub_backend.movies.models.MovieVote;
import com.cineclub_backend.cineclub_backend.movies.repositories.MovieVoteRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrudMovieVoteService {

  private final MovieVoteRepository movieVoteRepository;
  private final MongoTemplate mongoTemplate;

  public CrudMovieVoteService(
    MovieVoteRepository movieVoteRepository,
    MongoTemplate mongoTemplate
  ) {
    this.movieVoteRepository = movieVoteRepository;
    this.mongoTemplate = mongoTemplate;
  }

  @Transactional
  public void voteMovie(String movieId, String userId, MovieVote.VoteType type) {
    Optional<MovieVote> existingVoteOpt = movieVoteRepository.findByUserIdAndMovieId(
      userId,
      movieId
    );

    if (existingVoteOpt.isPresent()) {
      MovieVote existingVote = existingVoteOpt.get();

      if (existingVote.getType() == type) {
        movieVoteRepository.delete(existingVote);
        updateMovieCounters(movieId, type, -1);
      } else {
        updateMovieCounters(movieId, existingVote.getType(), -1);
        existingVote.setType(type);
        existingVote.setUpdatedAt(LocalDateTime.now());
        movieVoteRepository.save(existingVote);
        updateMovieCounters(movieId, type, 1);
      }
    } else {
      MovieVote newVote = new MovieVote();
      newVote.setUserId(userId);
      newVote.setMovieId(movieId);
      newVote.setType(type);
      movieVoteRepository.save(newVote);
      updateMovieCounters(movieId, type, 1);
    }
  }

  private void updateMovieCounters(String movieId, MovieVote.VoteType type, int increment) {
    Query query = new Query(Criteria.where("_id").is(movieId));
    Update update = new Update();

    if (type == MovieVote.VoteType.UP) {
      update.inc("up_votes", increment);
    } else {
      update.inc("down_votes", increment);
    }

    mongoTemplate.updateFirst(query, update, Movie.class);
  }
}
