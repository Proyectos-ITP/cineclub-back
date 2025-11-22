package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

@Data
public class UpdateMovieDto {

  private int externalId;

  @Size(max = 255)
  @Schema(example = "Jon Doe")
  private String title;

  @Size(max = 1000)
  @Schema(
    example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.."
  )
  private String overview;

  @Schema(example = "[{'id': 1, 'name': 'Animation'}, {'id': 2, 'name': 'Comedy'}]")
  private String genres;

  @Schema(example = "2025-10-27")
  private Date releaseDate;

  @Schema(example = "/poster.jpg")
  private String posterPath;

  @Schema(example = "120")
  private int runtime;

  @Schema(example = "en")
  private String originalLanguage;
}
