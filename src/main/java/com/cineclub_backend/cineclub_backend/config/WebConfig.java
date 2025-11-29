package com.cineclub_backend.cineclub_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

  @Value("${cors.allowed-origins}")
  private String[] allowedOrigins;

  @Value("${cors.allowed-methods}")
  private String[] allowedMethods;

  @Value("${cors.allowed-headers}")
  private String[] allowedHeaders;

  @Value("${cors.allow-credentials}")
  private boolean allowCredentials;

  @Autowired
  private OpenApiCacheInterceptor openApiCacheInterceptor;

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
          .addMapping("/**")
          .allowedOrigins(allowedOrigins)
          .allowedMethods(allowedMethods)
          .allowedHeaders(allowedHeaders)
          .allowCredentials(allowCredentials);
      }

      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        registry
          .addInterceptor(openApiCacheInterceptor)
          .addPathPatterns("/v3/api-docs/**", "/docs/**");
      }
    };
  }
}
