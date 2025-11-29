package com.cineclub_backend.cineclub_backend.reviews.controllers;

import com.cineclub_backend.cineclub_backend.reviews.dots.CommentDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.CreateCommentDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.FindCommentsPagedDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.UpdateCommentDto;
import com.cineclub_backend.cineclub_backend.reviews.services.CrudCommentLikeService;
import com.cineclub_backend.cineclub_backend.reviews.services.CrudCommentService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Endpoints para gestionar comentarios de las reseñas")
public class CommentsController {

  @Autowired
  private CrudCommentService crudCommentService;

  @Autowired
  private CrudCommentLikeService crudCommentLikeService;

  @GetMapping("/{reviewId}")
  @Operation(
    summary = "Buscar comentarios por reseña",
    description = "Se buscan los comentarios de una reseña paginados"
  )
  public PagedResponseDto<CommentDto> findPagedByReviewId(
    @ParameterObject FindCommentsPagedDto findCommentsPagedDto,
    @PathVariable @Schema(
      description = "Id de la reseña",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String reviewId,
    @AuthenticationPrincipal String userId
  ) {
    Page<CommentDto> page = crudCommentService.findPagedByReviewId(
      reviewId,
      userId,
      findCommentsPagedDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }

  @PostMapping
  @Operation(summary = "Crear comentario", description = "Se crea un comentario para una reseña")
  public ResponseEntity<ApiResponse<String>> createComment(
    @RequestBody @Valid CreateCommentDto commentDto,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentService.createComment(commentDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("Comentario creado correctamente.", rowId)
    );
  }

  @PostMapping("/reply/{parentId}")
  @Operation(summary = "Crear respuesta", description = "Se crea una respuesta para un comentario")
  public ResponseEntity<ApiResponse<String>> createReply(
    @RequestBody @Valid CreateCommentDto commentDto,
    @PathVariable @Schema(
      description = "Id del comentario padre",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String parentId,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentService.createReply(commentDto, parentId, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("Respuesta creada correctamente.", rowId)
    );
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Eliminar comentario",
    description = "Se elimina un comentario de una reseña"
  )
  public ResponseEntity<ApiResponse<String>> deleteComment(
    @PathVariable @Schema(
      description = "Id del comentario",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentService.deleteComment(id, userId);
    return ResponseEntity.ok(ApiResponse.success("Comentario eliminado correctamente.", rowId));
  }

  @DeleteMapping("/reply/{id}")
  @Operation(
    summary = "Eliminar respuesta",
    description = "Se elimina una respuesta de un comentario"
  )
  public ResponseEntity<ApiResponse<String>> deleteReply(
    @PathVariable @Schema(
      description = "Id de la respuesta",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentService.deleteReply(id, userId);
    return ResponseEntity.ok(ApiResponse.success("Respuesta eliminada correctamente.", rowId));
  }

  @PatchMapping("/{id}")
  @Operation(
    summary = "Actualizar comentario",
    description = "Se actualiza un comentario de una reseña"
  )
  public ResponseEntity<ApiResponse<String>> updateComment(
    @PathVariable @Schema(
      description = "Id del comentario",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String id,
    @RequestBody UpdateCommentDto updateCommentDtop,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentService.updateComment(id, updateCommentDtop, userId);
    return ResponseEntity.ok(ApiResponse.success("Comentario actualizado correctamente.", rowId));
  }

  @PostMapping("/like/{id}")
  @Operation(summary = "Dar like a un comentario", description = "Se da like a un comentario")
  public ResponseEntity<ApiResponse<String>> likeComment(
    @PathVariable @Schema(
      description = "Id del comentario",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentLikeService.createCommentLike(id, userId);
    return ResponseEntity.ok(ApiResponse.success("Like agregado correctamente.", rowId));
  }

  @DeleteMapping("/like/{id}")
  @Operation(
    summary = "Quitar like de un comentario",
    description = "Se quita like a un comentario"
  )
  public ResponseEntity<ApiResponse<String>> unlikeComment(
    @PathVariable @Schema(
      description = "Id del comentario",
      example = "679f2c2c2c2c2c2c2c2c2c2c",
      nullable = false
    ) String id,
    @AuthenticationPrincipal String userId
  ) {
    String rowId = crudCommentLikeService.deleteCommentLike(id, userId);
    return ResponseEntity.ok(ApiResponse.success("", rowId));
  }
}
