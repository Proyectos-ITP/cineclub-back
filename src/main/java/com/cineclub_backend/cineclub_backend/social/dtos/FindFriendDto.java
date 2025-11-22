package com.cineclub_backend.cineclub_backend.social.dtos;

import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FindFriendDto extends PaginationDto {

  @Schema(description = "Nombre del amigo para filtrar", example = "John")
  private String name;
}
