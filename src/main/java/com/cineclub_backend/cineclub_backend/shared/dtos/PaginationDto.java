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

  @Schema(defaultValue = "id,asc")
  private String[] sort = { "id", "asc" };

  public Pageable toPageable() {
    int pageNumber = page > 0 ? page - 1 : 0;

    if (sort == null || sort.length < 2) {
      return PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    Sort.Direction direction = Sort.Direction.fromString(sort[1]);
    return PageRequest.of(pageNumber, size, Sort.by(direction, sort[0]));
  }
}
