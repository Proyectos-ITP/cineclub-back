package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCollectionItemDto {

  @NotBlank(message = "La pelicula no puede estar vacia")
  @Size(max = 255)
  @Schema(example = "123")
  private String movieId;
}
