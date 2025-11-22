package com.cineclub_backend.cineclub_backend.shared.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para redirigir /docs a la documentación personalizada de Scalar
 */
@Controller
public class DocsRedirectController {

  /**
   * Redirige /docs a la página HTML personalizada de Scalar con persistencia de token
   */
  @GetMapping("/docs")
  public String redirectToDocs() {
    return "forward:/api-docs.html";
  }
}
