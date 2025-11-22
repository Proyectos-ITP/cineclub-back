package com.cineclub_backend.cineclub_backend.reviews.controllers;

import com.cineclub_backend.cineclub_backend.reviews.dots.FindReviewPagedDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.ReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.ReviewResponseDto;
import com.cineclub_backend.cineclub_backend.reviews.services.CrudReviewService;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reviews", description = "Endpoints para gestionar reseñas")
public class ReviewsController {

  private final CrudReviewService crudReviewService;

  public ReviewsController(CrudReviewService crudReviewService) {
    this.crudReviewService = crudReviewService;
  }

  @GetMapping
  @Operation(summary = "Listar reseñas", description = "Obtiene la lista de reseñas")
  public PagedResponseDto<ReviewResponseDto> getPagedReviews(
    @ParameterObject FindReviewPagedDto findReviewPagedDto
  ) {
    Page<ReviewDto> reviews = crudReviewService.getPagedReviews(findReviewPagedDto);
    Page<ReviewResponseDto> responsePage = reviews.map(this::mapToResponseDto);
    return new PagedResponseDto<>(responsePage);
  }

  private ReviewResponseDto mapToResponseDto(ReviewDto dto) {
    ReviewResponseDto response = new ReviewResponseDto();
    response.setId(dto.getId());
    response.setTitle(dto.getTitle());
    response.setContent(dto.getContent());
    response.setReviewerName(dto.getReviewerName());
    response.setDirectorName(dto.getDirectorName());
    response.setGenres(null);
    response.setRating(dto.getRating() != null ? String.valueOf(dto.getRating()) : null);
    response.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null);
    response.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt().toString() : null);
    return response;
  }
}
