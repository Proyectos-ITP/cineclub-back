package com.cineclub_backend.cineclub_backend.shared.templates;

import java.util.Optional;

public class FriendsRequestTemplate extends EmailBaseTemplate {

  public static String friendRequestSent(
    String senderName,
    String receiverName,
    Optional<String> ctaUrl
  ) {
    String subject = "Nueva solicitud de amistad";
    String mainContent = String.format(
      "<strong>%s</strong> quiere ser tu amigo en CineClub+. " +
        "¡Acepta la solicitud para ver qué películas tienen en común!",
      senderName
    );
    String buttonText = "Ver solicitud";

    return createBaseEmailTemplate(subject, receiverName, mainContent, ctaUrl, buttonText, "👋");
  }

  public static String friendRequestAccepted(
    String senderName,
    String receiverName,
    Optional<String> ctaUrl
  ) {
    String subject = "¡Solicitud aceptada!";
    String mainContent = String.format(
      "<strong>%s</strong> ha aceptado tu solicitud. ¡Genial! " +
        "Ahora pueden compartir recomendaciones y ver sus colecciones.",
      receiverName
    );
    String buttonText = "Ver perfil de " + receiverName;

    return createBaseEmailTemplate(subject, senderName, mainContent, ctaUrl, buttonText, "🍿");
  }

  public static String friendRequestRejected(
    String senderName,
    String receiverName,
    Optional<String> ctaUrl
  ) {
    String subject = "Solicitud rechazada";
    String mainContent = String.format(
      "Lo sentimos, <strong>%s</strong> ha decidido no aceptar tu solicitud en este momento.",
      receiverName
    );
    String buttonText = "Buscar otros amigos";

    return createBaseEmailTemplate(subject, senderName, mainContent, ctaUrl, buttonText, "💔");
  }
}
