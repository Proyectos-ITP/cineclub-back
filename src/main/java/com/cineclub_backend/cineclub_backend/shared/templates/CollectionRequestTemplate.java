package com.cineclub_backend.cineclub_backend.shared.templates;

import java.util.Optional;

public class CollectionRequestTemplate extends EmailBaseTemplate {

  public static String collectionShared(
    String senderName,
    String receiverName,
    Optional<String> ctaUrl
  ) {
    String subject = "Te han compartido una colección de películas";
    String mainContent = String.format(
      "<strong>%s</strong> quiere compartir su colección de películas contigo. " +
        "¡Acepta para descubrir nuevas joyas cinematográficas y añadirlas a tu lista!",
      senderName
    );
    String buttonText = "Ver colección compartida";

    return createBaseEmailTemplate(subject, receiverName, mainContent, ctaUrl, buttonText, "🎁");
  }
}
