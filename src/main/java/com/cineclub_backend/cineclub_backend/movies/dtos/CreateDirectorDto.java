package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDirectorDto {

  @NotBlank(message = "El director no puede estar vacio")
  @Size(max = 255)
  @Schema(example = "Jon Doe")
  private String director;

  @NotBlank(message = "El movieId no puede estar vacio")
  @Size(max = 255)
  @Schema(example = "123")
  private String movieId;
}
