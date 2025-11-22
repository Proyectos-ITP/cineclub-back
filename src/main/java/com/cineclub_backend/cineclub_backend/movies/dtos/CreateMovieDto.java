package com.cineclub_backend.cineclub_backend.movies.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

@Data
public class CreateMovieDto {

  private int externalId;

  @NotBlank(message = "El titulo no puede estar vacio")
  @Size(max = 255)
  @Schema(example = "Jon Doe")
  private String title;

  @Size(max = 1000)
  @Schema(
    example = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.."
  )
  private String overview;

  @Schema(
    defaultValue = "Uncategorized",
    example = "[{'id': 1, 'name': 'Animation'}, {'id': 2, 'name': 'Comedy'}]"
  )
  private String genres;

  @Schema(example = "2025-10-27")
  private Date releaseDate;

  @Schema(defaultValue = "/poster.jpg")
  private String posterPath;

  @Schema(example = "120")
  private int runtime;

  @Schema(defaultValue = "en")
  private String originalLanguage;

  @NotBlank(message = "El director no puede estar vacio")
  @Size(max = 255)
  @Schema(example = "Jon Doe")
  private String director;
}
