package com.cineclub_backend.cineclub_backend.social.services;

import com.cineclub_backend.cineclub_backend.social.models.Friend;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendsRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.Neo4jClient;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class Neo4jSyncService {

  private static final Logger logger = LoggerFactory.getLogger(Neo4jSyncService.class);

  private final UserRepository userRepository;
  private final FriendsRepository friendsRepository;
  private final Neo4jClient neo4jClient;

  public Neo4jSyncService(
    UserRepository userRepository,
    FriendsRepository friendsRepository,
    Neo4jClient neo4jClient
  ) {
    this.userRepository = userRepository;
    this.friendsRepository = friendsRepository;
    this.neo4jClient = neo4jClient;
  }

  public void syncData() {
    logger.info("Starting Neo4j data sync...");

    int pageSize = 100;
    int page = 0;

    while (true) {
      Page<User> userPage = userRepository.findAll(PageRequest.of(page, pageSize));
      if (userPage.isEmpty()) {
        break;
      }

      for (User user : userPage) {
        neo4jClient.upsertUser(user);
      }
      logger.info("Synced users batch {} ({} users)", page, userPage.getNumberOfElements());
      page++;
    }

    page = 0;
    while (true) {
      Page<Friend> friendPage = friendsRepository.findAll(PageRequest.of(page, pageSize));
      if (friendPage.isEmpty()) {
        break;
      }

      for (Friend friend : friendPage) {
        neo4jClient.addFriendship(friend.getUserId(), friend.getFriendId());
      }
      logger.info(
        "Synced friendships batch {} ({} friendships)",
        page,
        friendPage.getNumberOfElements()
      );
      page++;
    }

    logger.info("Neo4j data sync completed.");
  }
}
