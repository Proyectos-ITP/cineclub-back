package com.cineclub_backend.cineclub_backend.reviews.dots;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentDto {

  @Size(min = 1, max = 500, message = "El contenido debe tener entre 1 y 500 caracteres")
  @Schema(description = "Contenido del comentario", example = "Excelente película", type = "String")
  private String content;
}
