package com.cineclub_backend.cineclub_backend.notifications.controllers;

import com.cineclub_backend.cineclub_backend.notifications.dtos.NotificationResponseDto;
import com.cineclub_backend.cineclub_backend.notifications.services.NotificationService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Endpoints para gestionar notificaciones")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(
    summary = "Obtener notificaciones",
    description = "Obtiene las notificaciones del usuario autenticado"
  )
  public PagedResponseDto<NotificationResponseDto> getUserNotifications(
    @ParameterObject PaginationDto findNotificationPagedDto,
    @AuthenticationPrincipal String userId
  ) {
    Page<NotificationResponseDto> response = notificationService.getUserNotifications(
      userId,
      findNotificationPagedDto.toPageable()
    );
    return new PagedResponseDto<>(
      new PageImpl<>(response.getContent(), response.getPageable(), response.getTotalElements())
    );
  }

  @GetMapping("/count")
  @Operation(
    summary = "Obtener cantidad de notificaciones",
    description = "Obtiene la cantidad de notificaciones del usuario autenticado"
  )
  public ResponseEntity<ApiResponse<Long>> getCount(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(ApiResponse.success(notificationService.getCount(userId)));
  }

  @PatchMapping("/{id}/read")
  @Operation(
    summary = "Marcar notificación como leida",
    description = "Marca una notificación como leida"
  )
  public ResponseEntity<Void> markAsRead(@PathVariable String id) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok().build();
  }
}
