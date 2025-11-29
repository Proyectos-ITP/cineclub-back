package com.cineclub_backend.cineclub_backend.reviews.dots;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentDto {

  @NotNull(message = "El contenido del comentario no puede estar vacío")
  @NotEmpty(message = "El contenido del comentario no puede estar vacío")
  @Size(
    min = 1,
    max = 500,
    message = "El contenido del comentario debe tener entre 1 y 500 caracteres"
  )
  @Schema(
    description = "Contenido del comentario",
    example = "Lorem ipsum dolor sit amet...",
    type = "String"
  )
  private String content;

  @NotNull(message = "Debes seleccionar una reseña")
  @NotEmpty(message = "Debes seleccionar una reseña")
  @Schema(description = "ID de la reseña", example = "64d5b5b5b5b5b5b5b5b5b5b5", type = "String")
  private String reviewId;
}
