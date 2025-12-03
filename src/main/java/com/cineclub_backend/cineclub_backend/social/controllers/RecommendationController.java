package com.cineclub_backend.cineclub_backend.social.controllers;

import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import com.cineclub_backend.cineclub_backend.social.services.FriendRecommendationService;
import com.cineclub_backend.cineclub_backend.social.services.Neo4jSyncService;
import com.cineclub_backend.cineclub_backend.users.dtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("recommendations")
@Tag(
  name = "Recommendations",
  description = "Endpoints de recomendaciones de amigos en base a grafos"
)
public class RecommendationController {

  private final FriendRecommendationService recommendationService;
  private final Neo4jSyncService syncService;

  public RecommendationController(
    FriendRecommendationService recommendationService,
    Neo4jSyncService syncService
  ) {
    this.recommendationService = recommendationService;
    this.syncService = syncService;
  }

  @PostMapping("/sync")
  public ResponseEntity<String> syncData() {
    syncService.syncData();
    return ResponseEntity.ok("Data sync started.");
  }

  @GetMapping
  @Operation(
    summary = "Obtener recomendaciones de amigos",
    description = "Obtiene las recomendaciones de amigos para el usuario autenticado"
  )
  public ResponseEntity<PagedResponseDto<UserDto>> getRecommendations(
    @AuthenticationPrincipal String userId,
    @ParameterObject PaginationDto paginationDto
  ) {
    var page = recommendationService.getRecommendations(userId, paginationDto.toPageable());
    return ResponseEntity.ok(new PagedResponseDto<>(page));
  }
}
