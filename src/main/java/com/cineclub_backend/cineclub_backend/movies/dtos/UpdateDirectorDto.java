package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDirectorDto {

  @Size(max = 255)
  @Schema(example = "Jon Doe")
  private String director;

  @Size(max = 255)
  @Schema(example = "123")
  private String movieId;
}
