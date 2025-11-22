package com.cineclub_backend.cineclub_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${server.port}")
  private String serverPort;

  @Value("${server.url}")
  private String serverUrl;

  @Bean
  public OpenAPI customOpenAPI() {
    final String securitySchemeName = "Bearer Authentication";

    return new OpenAPI()
      .info(
        new Info()
          .title("🎬 Cineclub Backend API")
          .description(
            """
            ## Bienvenido a la API de Cineclub

            Esta es una API REST completa para la gestión integral de un club de cine, incluyendo gestión de usuarios,
            películas, eventos, amistades y notificaciones en tiempo real.

            ### 📊 Diagrama de Arquitectura

            <div style="
              position: relative;
              display: inline-block;
              padding: 20px;
              border-radius: 8px;
              background: transparent;
              box-shadow: 0 2px 8px rgba(0,0,0,0.1);
              overflow: auto;
              max-width: 100%;
              ">

              <img
                id="zoom-img"
                src="/diagram.svg"
                style="
                  transform-origin: top left;
                  transition: transform 0.15s ease;
                  min-width: 1000px;
                  display: block;"
              />
            </div>

            <details>
            <summary><strong>🔐 Autenticación</strong></summary>

            Esta API utiliza **JWT (JSON Web Tokens)** para la autenticación. Para usar los endpoints protegidos:

            1. Haz clic en el botón **"Authorize"** 🔓 en la parte superior
            2. Ingresa tu token JWT (sin el prefijo 'Bearer')
            3. El token se guardará automáticamente en tu navegador
            4. No necesitarás volver a ingresarlo en futuras sesiones

            </details>

            <details>
            <summary><strong>🚀 Tecnologías</strong></summary>

            - **Framework**: Spring Boot 3.x
            - **Base de Datos**: MongoDB
            - **Caché**: Redis
            - **Autenticación**: JWT
            - **WebSockets**: Para notificaciones en tiempo real
            - **Email**: SMTP (Gmail)

            </details>

            <details>
            <summary><strong>📚 Recursos Principales</strong></summary>

            - **Users**: Gestión de usuarios y perfiles
            - **Movies**: Catálogo de películas
            - **Events**: Eventos y proyecciones
            - **Friends**: Sistema de amistades
            - **Notifications**: Notificaciones en tiempo real

            </details>

            <details>
            <summary><strong>💡 Notas Importantes</strong></summary>

            - Todos los endpoints devuelven respuestas en formato JSON
            - Las fechas utilizan formato ISO 8601
            - Los errores siguen el estándar RFC 7807 (Problem Details)

            </details>

            ---

            **¿Necesitas ayuda?** Contacta al equipo de desarrollo.
            """
          )
          .version("1.0.0")
          .contact(new Contact().name("Equipo Cineclub").email("soporte@cineclub.com"))
      )
      .addServersItem(
        new Server()
          .url(serverUrl.equals("http://localhost") ? "http://localhost:" + serverPort : serverUrl)
          .description(
            serverUrl.equals("http://localhost")
              ? "Servidor de desarrollo local"
              : "Servidor de producción"
          )
      )
      .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
      .components(
        new Components().addSecuritySchemes(
          securitySchemeName,
          new SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Token JWT de autenticación (sin prefijo 'Bearer')")
        )
      );
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
      .group("default")
      .packagesToScan(
        "com.cineclub_backend.cineclub_backend.users.controllers",
        "com.cineclub_backend.cineclub_backend.movies.controllers",
        "com.cineclub_backend.cineclub_backend.social.controllers",
        "com.cineclub_backend.cineclub_backend.shared.controllers"
      )
      .build();
  }
}
