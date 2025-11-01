package com.cineclub_backend.cineclub_backend.social.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cineclub_backend.cineclub_backend.social.models.Friend;

@Repository
public interface FriendsRepository extends MongoRepository<Friend, String> {

    Page<Friend> findByUserId(String userId, Pageable pageable);

    Optional<Friend> findByUserIdAndFriendId(String userId, String friendId);

    void deleteByUserIdAndFriendId(String userId, String friendId);
}
