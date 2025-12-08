package com.cineclub_backend.cineclub_backend.movies.controllers;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionRequestResponseDto;
import com.cineclub_backend.cineclub_backend.movies.services.CollectionRequestService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/collections/requests")
@Tag(name = "Collection Requests", description = "Endpoints para compartir colecciones")
public class CollectionRequestController {

  private final CollectionRequestService collectionRequestService;

  public CollectionRequestController(CollectionRequestService collectionRequestService) {
    this.collectionRequestService = collectionRequestService;
  }

  @PostMapping("/share/{friendId}")
  @Operation(
    summary = "Compartir colección",
    description = "Envía una solicitud para compartir tu colección con un amigo"
  )
  public ResponseEntity<ApiResponse<String>> shareCollection(
    @PathVariable String friendId,
    @AuthenticationPrincipal String userId
  ) {
    collectionRequestService.sendCollection(userId, friendId);
    return ResponseEntity.ok(
      ApiResponse.success("Haz compartido tu biblioteca con tu amigo", null)
    );
  }

  @GetMapping
  @Operation(
    summary = "Listar solicitudes pendientes",
    description = "Obtiene las solicitudes de colección pendientes"
  )
  public ResponseEntity<ApiResponse<List<CollectionRequestResponseDto>>> getPendingRequests(
    @AuthenticationPrincipal String userId
  ) {
    List<CollectionRequestResponseDto> requests = collectionRequestService.getPendingRequests(
      userId
    );
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @GetMapping("/sended")
  @Operation(
    summary = "Listar solicitudes enviadas",
    description = "Obtiene las solicitudes de colección enviadas"
  )
  public ResponseEntity<ApiResponse<List<CollectionRequestResponseDto>>> getSendedRequests(
    @AuthenticationPrincipal String userId
  ) {
    List<CollectionRequestResponseDto> requests = collectionRequestService.getSendedRequests(
      userId
    );
    return ResponseEntity.ok(ApiResponse.success(requests));
  }

  @PostMapping("/{requestId}/accept")
  @Operation(
    summary = "Aceptar solicitud",
    description = "Acepta una solicitud y fusiona las películas a tu colección"
  )
  public ResponseEntity<ApiResponse<String>> acceptRequest(
    @PathVariable String requestId,
    @AuthenticationPrincipal String userId
  ) {
    collectionRequestService.acceptRequest(requestId, userId);
    return ResponseEntity.ok(
      ApiResponse.success(
        "Bibliotecas fusionada exitosamente, ahora podrás encontrar las películas compartidas en tu biblioteca",
        null
      )
    );
  }

  @DeleteMapping("/{requestId}")
  @Operation(
    summary = "Rechazar solicitud",
    description = "Rechaza y elimina una solicitud de colección"
  )
  public ResponseEntity<ApiResponse<String>> rejectRequest(
    @PathVariable String requestId,
    @AuthenticationPrincipal String userId
  ) {
    collectionRequestService.rejectRequest(requestId, userId);
    return ResponseEntity.ok(ApiResponse.success("Haz rechazado la biblioteca de tu amigo", null));
  }
}
