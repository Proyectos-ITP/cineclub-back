package com.cineclub_backend.cineclub_backend.shared.templates;

import java.time.Year;
import java.util.Optional;

public class EmailBaseTemplate {

  protected static String createBaseEmailTemplate(
    String subject,
    String receiverName,
    String mainContent,
    Optional<String> ctaUrl,
    String ctaText,
    String emoji
  ) {
    int currentYear = Year.now().getValue();

    return """
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="color-scheme" content="dark">
        <meta name="supported-color-schemes" content="dark light">
        <title>%s</title>
        <!--[if mso]>
        <style type="text/css">
            body, table, td {font-family: Arial, sans-serif !important;}
            .fallback-bg {background-color: #0f0c29 !important;}
        </style>
        <![endif]-->
    </head>
    <body style="margin: 0; padding: 0; background-color: #0f0c29; font-family: 'Outfit', 'Helvetica Neue',
    Helvetica, Arial, sans-serif; color: #e5e5e5; -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;">

        <!-- Main Container Table -->
        <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%"
        style="background-color: #0f0c29; padding: 40px 0;">
            <tr>
                <td align="center">
                    <!-- Content Wrapper -->
                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="600"
                    style="max-width: 600px; width: 100%%; background: linear-gradient(145deg, #240b36, #0f0c29);
                    border-radius: 16px; overflow: hidden; box-shadow: 0 8px 32px rgba(0,0,0,0.5);
                    border: 1px solid rgba(255,255,255,0.1);" class="container">

                        <!-- HEADER -->
                        <tr>
                            <td style="background: linear-gradient(90deg, #6a11cb 0%%, #2575fc 100%%);
                            padding: 40px 20px; text-align: center;">
                                <h1 style="margin: 0; font-size: 32px; color: #ffffff; letter-spacing: 2px;
                                text-transform: uppercase; text-shadow: 0 2px 4px rgba(0,0,0,0.3); font-weight: 700;
                                mso-line-height-rule: exactly;">CINECLUB+</h1>
                            </td>
                        </tr>

                        <!-- CONTENT -->
                        <tr>
                            <td style="padding: 40px 30px; background: rgba(255, 255, 255, 0.02);">
                                <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%">
                                    <!-- Emoji -->
                                    <tr>
                                        <td style="text-align: center; font-size: 48px; line-height: 1;
                                        padding-bottom: 20px; filter: drop-shadow(0 0 10px rgba(106, 17, 203, 0.5));">
                                            %s
                                        </td>
                                    </tr>
                                    <!-- Subject -->
                                    <tr>
                                        <td style="text-align: center; padding-bottom: 25px;">
                                            <h2 style="color: #ffffff; font-size: 26px; margin: 0; font-weight: 600;
                                            line-height: 1.3; mso-line-height-rule: exactly;">%s</h2>
                                        </td>
                                    </tr>
                                    <!-- Salutation -->
                                    <tr>
                                        <td style="font-size: 17px; color: #d1d1d1; line-height: 1.6;
                                        padding-bottom: 20px;">
                                            Hola <strong style="color: #ffffff;">%s</strong>,
                                        </td>
                                    </tr>
                                    <!-- Main Content -->
                                    <tr>
                                        <td style="font-size: 16px; color: #e0e0e0; line-height: 1.8;
                                        padding-bottom: 35px; background: rgba(0,0,0,0.2); padding: 20px;
                                        border-radius: 8px;">
                                            %s
                                        </td>
                                    </tr>
                                    <!-- CTA Button -->
                                    %s
                                    <!-- Signature -->
                                    <tr>
                                        <td style="padding-top: 45px; border-top: 1px solid rgba(255,255,255,0.1);
                                        text-align: center;">
                                            <p style="font-size: 14px; color: #888; margin: 0; line-height: 1.5;">
                                                Nos vemos en el cine,<br>
                                                <strong style="color: #a18cd1;">El equipo de CineClub+</strong>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <!-- FOOTER -->
                        <tr>
                            <td style="background-color: #0b091a; padding: 25px; text-align: center; font-size: 12px;
                            color: #666; border-top: 1px solid rgba(255,255,255,0.05);">
                                <p style="margin: 5px 0; line-height: 1.4;">&copy; %d CineClub+.
                                Todos los derechos reservados.</p>
                                <p style="margin: 0; line-height: 1.4;">¿No solicitaste esto?
                                Puedes ignorar este correo.</p>
                            </td>
                        </tr>

                    </table>
                    <!--[if mso]>
                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="600">
                        <tr><td style="padding-top: 20px;"></td></tr>
                    </table>
                    <![endif]-->
                </td>
            </tr>
        </table>

    </body>
    </html>
    """.formatted(
        subject,
        emoji,
        subject,
        receiverName,
        mainContent,
        ctaUrl.isPresent()
          ? """
            <tr>
                <td style="text-align: center; padding: 10px 0 20px 0;">
                    <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto;">
                        <tr>
                            <td style="background: linear-gradient(90deg, #6a11cb 0%%, #2575fc 100%%);
                            border-radius: 50px; box-shadow: 0 4px 15px rgba(106, 17, 203, 0.4);">
                                <a href="%s" style="background: linear-gradient(90deg, #6a11cb 0%%, #2575fc 100%%);
                                color: #ffffff; padding: 16px 36px; text-decoration: none; border-radius: 50px;
                                font-weight: bold; font-size: 16px; display: inline-block; mso-padding-alt: 0;">
                                    <!--[if mso]><i style="mso-font-width: 100%%; mso-text-raise: 16pt;">&nbsp;</i>
                                    <![endif]-->%s<!--[if mso]><i style="mso-font-width: 100%%;
                                    mso-text-raise: -16pt;">&nbsp;</i><![endif]-->
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            """.formatted(ctaUrl.get(), ctaText)
          : "<tr><td></td></tr>",
        currentYear
      );
  }
}
