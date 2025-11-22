package com.cineclub_backend.cineclub_backend.social.controllers;

import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.social.dtos.FindFriendDto;
import com.cineclub_backend.cineclub_backend.social.dtos.FriendResponseDto;
import com.cineclub_backend.cineclub_backend.social.services.CrudFriendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social/friends")
@Tag(name = "Friends", description = "Endpoints para gestionar amigos")
public class FriendsController {

  private final CrudFriendsService crudFriendsService;

  public FriendsController(CrudFriendsService crudFriendsService) {
    this.crudFriendsService = crudFriendsService;
  }

  @GetMapping
  @Operation(
    summary = "Listar amigos paginados",
    description = "Obtiene la lista de amigos del usuario de forma paginada con información del usuario"
  )
  public PagedResponseDto<FriendResponseDto> getFriendsPaginated(
    @AuthenticationPrincipal String userId,
    @ParameterObject FindFriendDto findFriendDto
  ) {
    Page<FriendResponseDto> page = crudFriendsService.getFriendsPaginated(
      userId,
      findFriendDto.getName(),
      findFriendDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }

  @DeleteMapping("/{friendId}")
  @Operation(
    summary = "Eliminar amigo",
    description = "Elimina la relación de amistad con un usuario"
  )
  public ResponseEntity<ApiResponse<String>> deleteFriend(
    @AuthenticationPrincipal String userId,
    @PathVariable String friendId
  ) {
    crudFriendsService.deleteFriend(userId, friendId);
    return ResponseEntity.ok(ApiResponse.success("Amistad eliminada exitosamente", null));
  }
}
