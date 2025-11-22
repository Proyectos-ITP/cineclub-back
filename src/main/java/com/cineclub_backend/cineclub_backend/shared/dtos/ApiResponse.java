package com.cineclub_backend.cineclub_backend.shared.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private int status;
  private String message;
  private String error;
  private T data;

  public ApiResponse(int status, T data) {
    this.status = status;
    this.data = data;
    this.message = null;
    this.error = null;
  }

  public ApiResponse(int status, String message, T data) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.error = null;
  }

  public ApiResponse(int status, String error, T data, boolean isError) {
    this.status = status;
    this.error = error;
    this.data = data;
    this.message = null;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(200, data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(200, message, data);
  }

  public static <T> ApiResponse<T> created(String message, T data) {
    return new ApiResponse<>(201, message, data);
  }

  public static <T> ApiResponse<T> error(int status, String error) {
    return new ApiResponse<>(status, error, null, true);
  }

  public static <T> ApiResponse<T> notFound(String error) {
    return new ApiResponse<>(404, error, null, true);
  }

  public static <T> ApiResponse<T> badRequest(String error) {
    return new ApiResponse<>(400, error, null, true);
  }

  public static <T> ApiResponse<T> unauthorized(String error) {
    return new ApiResponse<>(401, error, null, true);
  }

  public static <T> ApiResponse<T> serverError(String error) {
    return new ApiResponse<>(500, error, null, true);
  }
}
