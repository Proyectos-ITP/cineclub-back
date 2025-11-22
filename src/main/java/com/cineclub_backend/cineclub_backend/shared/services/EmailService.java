package com.cineclub_backend.cineclub_backend.shared.services;

import jakarta.mail.MessagingException; // La excepción que debemos manejar
import jakarta.mail.internet.MimeMessage; // El tipo de mensaje correcto
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// --- IMPORTS NECESARIOS ---
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper; // El helper clave
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  /**
   * Envía un correo electrónico con contenido HTML.
   * @param to El destinatario.
   * @param subject El asunto del correo.
   * @param htmlBody El string que contiene el HTML del correo.
   */
  public void sendEmail(String to, String subject, String htmlBody) {
    try {
      // 1. Crear un MimeMessage en lugar de SimpleMailMessage
      // MimeMessage es el que puede manejar HTML, adjuntos, etc.
      MimeMessage mimeMessage = mailSender.createMimeMessage();

      // 2. Usar MimeMessageHelper para configurar el email
      // El 'true' en el constructor indica que el mensaje será "multipart" (útil para HTML con adjuntos).
      // "UTF-8" asegura que los caracteres especiales (como tildes o emojis) se vean bien.
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject(subject);

      // 3. ¡ESTA ES LA PARTE CLAVE!
      // Usamos helper.setText(body, isHtml)
      // Al pasar 'true' como segundo argumento, le decimos a Spring
      // que el string 'htmlBody' debe ser interpretado como HTML (Content-Type: text/html).
      helper.setText(htmlBody, true);

      // (Opcional pero recomendado) Configura quién envía el correo.
      // Deberías tener esto en tu application.properties (ej. spring.mail.username)
      // helper.setFrom("no-reply@cineclub.com");

      // 4. Enviar el MimeMessage
      mailSender.send(mimeMessage);
      log.info("Email HTML enviado exitosamente a: {}", to);
    } catch (MessagingException e) {
      // MessagingException es la excepción específica que MimeMessageHelper puede lanzar
      log.error("Error enviando email HTML a: {}. Error: {}", to, e.getMessage());
      throw new RuntimeException("Fallo al enviar el email", e);
    } catch (Exception e) {
      // Una captura genérica por si algo más falla
      log.error("Error inesperado enviando email a: {}. Error: {}", to, e.getMessage());
      throw new RuntimeException("Fallo inesperado al enviar el email", e);
    }
  }
}
