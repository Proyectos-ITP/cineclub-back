package com.cineclub_backend.cineclub_backend.social.controllers;

import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.social.dtos.CreateFriendDto;
import com.cineclub_backend.cineclub_backend.social.models.FriendRequest;
import com.cineclub_backend.cineclub_backend.social.services.CrudFriendsRequestsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social/friend-requests")
@Tag(name = "Friend Requests", description = "Endpoints para gestionar solicitudes de amistad")
public class FriendRequestsController {

  private final CrudFriendsRequestsService crudFriendsRequests;

  public FriendRequestsController(CrudFriendsRequestsService crudFriendsRequests) {
    this.crudFriendsRequests = crudFriendsRequests;
  }

  @PostMapping
  @Operation(
    summary = "Enviar solicitud de amistad",
    description = "Envía una solicitud de amistad a otro usuario"
  )
  public ResponseEntity<ApiResponse<FriendRequest>> sendFriendRequest(
    @Valid @RequestBody CreateFriendDto createFriendDto,
    @AuthenticationPrincipal String userId
  ) {
    FriendRequest friendRequest = crudFriendsRequests.sendFriendRequest(
      userId,
      createFriendDto.getFriendId()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("Solicitud de amistad enviada exitosamente", friendRequest)
    );
  }

  @PutMapping("/accept/{senderId}")
  @Operation(
    summary = "Aceptar solicitud de amistad",
    description = "Acepta una solicitud de amistad recibida y crea la relación de amistad"
  )
  public ResponseEntity<ApiResponse<String>> acceptFriendRequest(
    @PathVariable String senderId,
    @AuthenticationPrincipal String userId
  ) {
    crudFriendsRequests.acceptFriendRequest(userId, senderId);
    return ResponseEntity.ok(
      ApiResponse.success("Solicitud de amistad aceptada exitosamente. Ahora son amigos.", null)
    );
  }

  @PutMapping("/reject/{senderId}")
  @Operation(
    summary = "Rechazar solicitud de amistad",
    description = "Rechaza una solicitud de amistad recibida"
  )
  public ResponseEntity<ApiResponse<String>> rejectFriendRequest(
    @PathVariable String senderId,
    @AuthenticationPrincipal String userId
  ) {
    crudFriendsRequests.rejectFriendRequest(userId, senderId);
    return ResponseEntity.ok(
      ApiResponse.success("Solicitud de amistad rechazada exitosamente", null)
    );
  }

  @GetMapping("/received")
  @Operation(
    summary = "Ver solicitudes recibidas",
    description = "Obtiene todas las solicitudes de amistad recibidas pendientes"
  )
  public ResponseEntity<ApiResponse<List<FriendRequest>>> getReceivedRequests(
    @AuthenticationPrincipal String userId
  ) {
    List<FriendRequest> requests = crudFriendsRequests.getReceivedRequests(userId);
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @GetMapping("/sent")
  @Operation(
    summary = "Ver solicitudes enviadas",
    description = "Obtiene todas las solicitudes de amistad enviadas por el usuario"
  )
  public ResponseEntity<ApiResponse<List<FriendRequest>>> getSentRequests(
    @AuthenticationPrincipal String userId
  ) {
    List<FriendRequest> requests = crudFriendsRequests.getSentRequests(userId);
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @DeleteMapping("/cancel/{receiverId}")
  @Operation(
    summary = "Cancelar solicitud de amistad",
    description = "Cancela una solicitud de amistad enviada"
  )
  public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
    @PathVariable String receiverId,
    @AuthenticationPrincipal String userId
  ) {
    crudFriendsRequests.cancelFriendRequest(userId, receiverId);
    return ResponseEntity.ok(
      ApiResponse.success("Solicitud de amistad cancelada exitosamente", null)
    );
  }
}
