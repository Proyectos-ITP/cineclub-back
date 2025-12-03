package com.cineclub_backend.cineclub_backend.social.services;

import com.cineclub_backend.cineclub_backend.social.repositories.Neo4jClient;
import com.cineclub_backend.cineclub_backend.users.dtos.UserDto;
import com.cineclub_backend.cineclub_backend.users.services.CrudUserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FriendRecommendationService {

  private final Neo4jClient neo4jClient;
  private final CrudUserService crudUserService;

  public FriendRecommendationService(Neo4jClient neo4jClient, CrudUserService crudUserService) {
    this.neo4jClient = neo4jClient;
    this.crudUserService = crudUserService;
  }

  public Page<UserDto> getRecommendations(String userId, Pageable pageable) {
    Page<String> neo4jPage = neo4jClient.getRecommendations(userId, pageable);

    if (neo4jPage.isEmpty()) {
      return Page.empty(pageable);
    }

    List<String> userIds = neo4jPage.getContent();
    List<UserDto> userDetails = crudUserService.getUsersDetails(userId, userIds);
    Map<String, UserDto> userMap = userDetails
      .stream()
      .collect(Collectors.toMap(UserDto::getId, Function.identity()));

    List<UserDto> sortedUsers = userIds
      .stream()
      .map(userMap::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    return new PageImpl<>(sortedUsers, pageable, neo4jPage.getTotalElements());
  }
}
