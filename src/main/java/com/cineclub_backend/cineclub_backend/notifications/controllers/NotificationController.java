package com.cineclub_backend.cineclub_backend.notifications.controllers;

import com.cineclub_backend.cineclub_backend.notifications.models.Notification;
import com.cineclub_backend.cineclub_backend.notifications.services.NotificationService;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public PagedResponseDto<Notification> getUserNotifications(
    @ParameterObject PaginationDto findNotificationPagedDto,
    @AuthenticationPrincipal String userId
  ) {
    Page<Notification> response = notificationService.getUserNotifications(
      userId,
      findNotificationPagedDto.toPageable()
    );
    return new PagedResponseDto<>(
      new PageImpl<>(response.getContent(), response.getPageable(), response.getTotalElements())
    );
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable String id) {
    notificationService.markAsRead(id);
    return ResponseEntity.ok().build();
  }
}
