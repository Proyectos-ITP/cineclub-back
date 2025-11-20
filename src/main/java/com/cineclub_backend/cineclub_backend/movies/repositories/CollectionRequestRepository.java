package com.cineclub_backend.cineclub_backend.movies.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cineclub_backend.cineclub_backend.movies.models.CollectionRequest;

@Repository
public interface CollectionRequestRepository extends MongoRepository<CollectionRequest, String> {
    
    List<CollectionRequest> findByReceiverIdAndStatus(String receiverId, String status);
    
    Optional<CollectionRequest> findBySenderIdAndReceiverIdAndStatus(String senderId, String receiverId, String status);
}
