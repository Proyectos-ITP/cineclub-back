package com.cineclub_backend.cineclub_backend.movies.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cineclub_backend.cineclub_backend.movies.models.Collection;

@Repository
public interface CollectionRepository extends MongoRepository<Collection, String> {
    Optional<Collection> findByUserId(String userId);
}
