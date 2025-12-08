package com.cineclub_backend.cineclub_backend.movies.repositories;

import com.cineclub_backend.cineclub_backend.movies.models.CollectionRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRequestRepository extends MongoRepository<CollectionRequest, String> {
  List<CollectionRequest> findByReceiverIdAndStatus(String receiverId, String status);

  List<CollectionRequest> findBySenderIdAndStatus(String senderId, String status);

  Optional<CollectionRequest> findBySenderIdAndReceiverIdAndStatus(
    String senderId,
    String receiverId,
    String status
  );
}
