package com.cineclub_backend.cineclub_backend.shared.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PaginationDto {

  @Schema(defaultValue = "1")
  private int page = 1;

  @Schema(defaultValue = "10")
  private int size = 10;

  @Schema(defaultValue = "created_at,asc")
  private String[] sort = { "created_at", "asc" };

  public Pageable toPageable() {
    int pageNumber = page > 0 ? page - 1 : 0;

    if (sort == null || sort.length < 2) {
      return PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "created_at"));
    }

    Sort.Direction direction = Sort.Direction.fromString(sort[1]);
    return PageRequest.of(pageNumber, size, Sort.by(direction, sort[0]));
  }
}
