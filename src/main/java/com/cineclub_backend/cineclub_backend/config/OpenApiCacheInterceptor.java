package com.cineclub_backend.cineclub_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que agrega caché HTTP headers al endpoint de OpenAPI
 * para que el browser cachee la respuesta por 1 hora
 */
@Component
public class OpenApiCacheInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) {
    response.setHeader("Cache-Control", "public, max-age=3600");
    response.setHeader("Vary", "Accept-Encoding");
    return true;
  }
}
