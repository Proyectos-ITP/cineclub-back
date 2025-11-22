package com.cineclub_backend.cineclub_backend.shared.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

  @NotBlank(message = "El destinatario es obligatorio")
  @Email(message = "El email debe ser válido")
  private String to;

  @NotBlank(message = "El asunto es obligatorio")
  private String subject;

  @NotBlank(message = "El mensaje es obligatorio")
  private String text;
}
