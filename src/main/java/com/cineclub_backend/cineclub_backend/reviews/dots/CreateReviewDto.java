package com.cineclub_backend.cineclub_backend.reviews.dots;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewDto {

  @NotNull(message = "El contenido de la reseña no puede estar vacío")
  @NotEmpty(message = "El contenido de la reseña no puede estar vacío")
  @Size(
    min = 1,
    max = 500,
    message = "El contenido de la reseña debe tener entre 1 y 500 caracteres"
  )
  @Schema(
    description = "Contenido de la reseña",
    example = "Lorem ipsum dolor sit amet...",
    type = "String"
  )
  private String content;

  @NotNull(message = "La calificación no puede estar vacía")
  @Min(value = 1, message = "La calificación debe ser mayor o igual a 1")
  @Max(value = 5, message = "La calificación debe ser menor o igual a 5")
  @Schema(description = "Calificación de la reseña", example = "5", type = "Integer")
  private Integer rating;

  @NotNull(message = "Debes seleccionar una película")
  @NotEmpty(message = "Debes seleccionar una película")
  @Schema(description = "ID de la película", example = "64d5b5b5b5b5b5b5b5b5b5b5", type = "String")
  private String movieId;
}
