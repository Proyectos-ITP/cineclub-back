package com.cineclub_backend.cineclub_backend.movies.dtos;

import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FindCollectionPagedDto extends PaginationDto {

  @Schema(example = "Toy Story")
  private String title;
}
