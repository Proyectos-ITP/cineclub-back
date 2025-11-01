package com.cineclub_backend.cineclub_backend.shared.templates;

import java.time.Year;
import java.util.Optional;

public class FriendsRequestTemplate {

    public static String friendRequestSent(String senderName, String receiverName, Optional<String> ctaUrl) {
        String subject = "👋 Nueva solicitud de amistad";
        String mainContent = String.format(
                "<strong>%s</strong> te ha enviado una solicitud de amistad. ¡No dejes que se te escape esta conexión cinéfila!",
                senderName
        );
        String buttonText = "Ver solicitud";

        return createBaseEmailTemplate(subject, receiverName, mainContent, ctaUrl, buttonText, "🎬");
    }

    public static String friendRequestAccepted(String senderName, String receiverName, Optional<String> ctaUrl) {
        String subject = "🍿 ¡Solicitud de amistad aceptada!";
        String mainContent = String.format(
                "<strong>%s</strong> ha aceptado tu solicitud de amistad. ¡Preparen las palomitas, que se viene maratón de películas!",
                receiverName
        );
        String buttonText = "Ver perfil de " + receiverName;

        return createBaseEmailTemplate(subject, senderName, mainContent, ctaUrl, buttonText, "⭐");
    }

    public static String friendRequestRejected(String senderName, String receiverName, Optional<String> ctaUrl) {
        String subject = "💔 Solicitud de amistad rechazada";
        String mainContent = String.format(
                "<strong>%s</strong> ha rechazado tu solicitud de amistad. Pero tranquilo, hay muchas más historias por descubrir.",
                receiverName
        );
        String buttonText = "Buscar nuevos amigos";

        return createBaseEmailTemplate(subject, senderName, mainContent, ctaUrl, buttonText, "🎥");
    }

    private static String createBaseEmailTemplate(String subject, String receiverName, String mainContent, Optional<String> ctaUrl, String ctaText, String emoji) {
        int currentYear = Year.now().getValue();

        return """
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>%s</title>
        </head>
        <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f3f3f3; margin: 0; padding: 40px 0;">

            <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                
                <!-- HEADER -->
                <div style="background: linear-gradient(90deg, #E50914, #B20710); padding: 25px 20px; text-align: center; color: #fff;">
                    <h1 style="margin: 0; font-size: 26px;">CineClub %s</h1>
                </div>

                <!-- CONTENT -->
                <div style="padding: 30px;">
                    <h2 style="color: #222; font-size: 22px; margin-top: 0;">%s</h2>
                    <p style="font-size: 16px; color: #555;">¡Hola, <strong>%s</strong>!</p>
                    <p style="font-size: 15px; color: #555; line-height: 1.6;">%s</p>

                    %s

                    <p style="margin-top: 25px; font-size: 14px; color: #777; text-align: center;">
                        — El equipo de <strong>CineClub</strong> 🎬
                    </p>
                </div>

                <!-- FOOTER -->
                <div style="background-color: #fafafa; padding: 15px 20px; text-align: center; font-size: 12px; color: #999;">
                    <p style="margin: 5px 0;">&copy; %d CineClub. Todos los derechos reservados.</p>
                    <p style="margin: 0;">Recibiste este correo por una notificación de tu cuenta.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                subject,
                emoji,
                subject,
                receiverName,
                mainContent,
                ctaUrl.isPresent()
                    ? "<div style='text-align: center; margin-top: 30px;'>"
                        + "<a href='" + ctaUrl.get() + "' "
                        + "style='background-color: #E50914; color: #fff; padding: 12px 25px; "
                        + "text-decoration: none; border-radius: 6px; font-weight: bold; "
                        + "font-size: 15px; display: inline-block;'>"
                        + ctaText + "</a></div>"
                    : "",
                currentYear
        );
    }
}
