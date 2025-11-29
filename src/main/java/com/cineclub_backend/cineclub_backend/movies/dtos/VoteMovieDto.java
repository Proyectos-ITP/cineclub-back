package com.cineclub_backend.cineclub_backend.movies.dtos;

import com.cineclub_backend.cineclub_backend.movies.models.MovieVote;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteMovieDto {

  @NotNull(message = "El tipo de voto es obligatorio")
  @Schema(description = "Tipo de voto", example = "UP", nullable = false)
  private MovieVote.VoteType type;
}
