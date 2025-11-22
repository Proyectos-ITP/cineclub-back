package com.cineclub_backend.cineclub_backend.social.repositories;

import com.cineclub_backend.cineclub_backend.social.models.FriendRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
  List<FriendRequest> findByReceiverId(String receiverId);
  List<FriendRequest> findBySenderId(String senderId);
  Optional<FriendRequest> findBySenderIdAndReceiverId(String senderId, String receiverId);
}
