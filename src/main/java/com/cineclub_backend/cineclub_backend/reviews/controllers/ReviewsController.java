package com.cineclub_backend.cineclub_backend.reviews.controllers;

import com.cineclub_backend.cineclub_backend.reviews.dots.CreateReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.FindReviewPagedDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.ReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.UpdateReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.services.CrudReviewLikeService;
import com.cineclub_backend.cineclub_backend.reviews.services.CrudReviewService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Endpoints para gestionar reseñas")
public class ReviewsController {

  private final CrudReviewService crudReviewService;
  private final CrudReviewLikeService crudReviewLikeService;

  public ReviewsController(
    CrudReviewService crudReviewService,
    CrudReviewLikeService crudReviewLikeService
  ) {
    this.crudReviewService = crudReviewService;
    this.crudReviewLikeService = crudReviewLikeService;
  }

  @GetMapping
  @Operation(summary = "Listar reseñas", description = "Obtiene la lista de reseñas")
  public PagedResponseDto<ReviewDto> getPagedReviews(
    @ParameterObject FindReviewPagedDto findReviewPagedDto,
    @AuthenticationPrincipal String userId
  ) {
    Page<ReviewDto> reviews = crudReviewService.getPagedReviews(
      findReviewPagedDto,
      findReviewPagedDto.getUserId(),
      userId
    );
    return new PagedResponseDto<>(reviews);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Obtener reseña por ID", description = "Obtiene una reseña por su ID")
  public ResponseEntity<ApiResponse<ReviewDto>> getReviewById(
    @PathVariable String id,
    @AuthenticationPrincipal String userId
  ) {
    ReviewDto reviewDto = crudReviewService.getReviewById(id, userId);
    return ResponseEntity.ok(ApiResponse.success(reviewDto));
  }

  @GetMapping("/user")
  @Operation(
    summary = "Listar reseñas por usuario",
    description = "Obtiene la lista de reseñas por usuario"
  )
  public PagedResponseDto<ReviewDto> getPagedReviewsByUserId(
    @ParameterObject FindReviewPagedDto findReviewPagedDto,
    @AuthenticationPrincipal String userId
  ) {
    Page<ReviewDto> reviews = crudReviewService.getPagedReviews(findReviewPagedDto, userId, userId);
    return new PagedResponseDto<>(reviews);
  }

  @PostMapping
  @Operation(summary = "Crear reseña", description = "Crea una nueva reseña")
  public ResponseEntity<ApiResponse<String>> createReview(
    @RequestBody @Valid CreateReviewDto createReviewDto,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudReviewService.createReview(createReviewDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("La reseña se agregó a tu biblioteca.", rowId)
    );
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Actualizar reseña", description = "Actualiza una reseña existente")
  public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
    @PathVariable String id,
    @RequestBody UpdateReviewDto updateReviewDto,
    @AuthenticationPrincipal String userId
  ) {
    ReviewDto updatedReview = crudReviewService.updateReview(id, updateReviewDto, userId);
    return ResponseEntity.ok(
      ApiResponse.success("La reseña se actualizó correctamente.", updatedReview)
    );
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Eliminar reseña", description = "Elimina una reseña existente")
  public ResponseEntity<ApiResponse<String>> deleteReview(
    @PathVariable String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudReviewService.deleteReview(id, userId);
    return ResponseEntity.ok(ApiResponse.success("La reseña se eliminó correctamente.", rowId));
  }

  @PostMapping("/like/{id}")
  @Operation(summary = "Dar like a una reseña", description = "Da like a una reseña")
  public ResponseEntity<ApiResponse<String>> likeReview(
    @PathVariable String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudReviewLikeService.createLikeReview(id, userId);
    return ResponseEntity.ok(ApiResponse.success("", rowId));
  }

  @DeleteMapping("/like/{id}")
  @Operation(summary = "Eliminar like de una reseña", description = "Elimina el like de una reseña")
  public ResponseEntity<ApiResponse<String>> dislikeReview(
    @PathVariable String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudReviewLikeService.removeLikeReview(id, userId);
    return ResponseEntity.ok(ApiResponse.success("", rowId));
  }
}
