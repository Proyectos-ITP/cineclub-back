package com.cineclub_backend.cineclub_backend.config;

import org.springdoc.core.service.OpenAPIService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Controller personalizado que cachea la generación de OpenAPI spec
 */
@RestController
@Configuration
public class OpenApiCacheConfig {

    private final OpenAPIService openAPIService;

    public OpenApiCacheConfig(OpenAPIService openAPIService) {
        this.openAPIService = openAPIService;
    }

    /**
     * Endpoint cacheado para la spec de OpenAPI
     * Cachea en Redis por 1 hora (configurado en application.properties)
     */
    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value = "openapi-spec", key = "'v3-api-docs'")
    @Hidden // No mostrar este endpoint en la documentación
    public String openapiJson() {
        return openAPIService.build(null).toString();
    }
}
