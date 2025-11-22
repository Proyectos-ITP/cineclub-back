package com.cineclub_backend.cineclub_backend.social.repositories;

import com.cineclub_backend.cineclub_backend.social.models.Friend;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendsRepository extends MongoRepository<Friend, String> {
  Page<Friend> findByUserId(String userId, Pageable pageable);

  Optional<Friend> findByUserIdAndFriendId(String userId, String friendId);

  void deleteByUserIdAndFriendId(String userId, String friendId);
}
