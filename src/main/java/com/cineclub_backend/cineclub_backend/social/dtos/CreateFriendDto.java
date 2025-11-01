package com.cineclub_backend.cineclub_backend.social.dtos;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Data
public class CreateFriendDto {

    @NotEmpty(message = "El ID del amigo no puede estar vacío")
    @Schema(description = "ID del amigo", required = true)
    private String friendId;
}
