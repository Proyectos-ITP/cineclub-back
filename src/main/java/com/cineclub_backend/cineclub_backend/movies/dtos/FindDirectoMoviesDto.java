package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FindDirectoMoviesDto {

  @NotEmpty(message = "Director is required")
  @Schema(example = "Jon Doe")
  private String director;
}
