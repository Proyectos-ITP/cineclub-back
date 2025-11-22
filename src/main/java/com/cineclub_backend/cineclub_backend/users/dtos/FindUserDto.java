package com.cineclub_backend.cineclub_backend.users.dtos;

import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FindUserDto extends PaginationDto {

  @Schema(description = "Nombre del usuario para filtrar", example = "John")
  private String name;

  @Schema(description = "Email del usuario para filtrar", example = "john@example.com")
  private String email;
}
