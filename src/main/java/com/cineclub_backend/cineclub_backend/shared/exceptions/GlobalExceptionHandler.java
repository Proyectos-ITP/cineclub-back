package com.cineclub_backend.cineclub_backend.shared.exceptions;

import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import java.util.NoSuchElementException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(NoSuchElementException ex) {
    ApiResponse<Void> response = ApiResponse.notFound(ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
    IllegalArgumentException ex
  ) {
    ApiResponse<Void> response = ApiResponse.badRequest(ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    ApiResponse<Void> response = ApiResponse.serverError(ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message = String.format(
      "El campo '%s' debe ser del tipo '%s'.",
      ex.getName(),
      ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido"
    );

    return ResponseEntity.badRequest().body(ApiResponse.badRequest(message));
  }
}
