package com.cineclub_backend.cineclub_backend.shared.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject(subject);

      helper.setText(htmlBody, true);

      mailSender.send(mimeMessage);
      log.info("Email HTML enviado exitosamente a: {}", to);
    } catch (MessagingException e) {
      log.error("Error enviando email HTML a: {}. Error: {}", to, e.getMessage());
      throw new RuntimeException("Fallo al enviar el email", e);
    } catch (Exception e) {
      log.error("Error inesperado enviando email a: {}. Error: {}", to, e.getMessage());
      throw new RuntimeException("Fallo inesperado al enviar el email", e);
    }
  }
}
