package com.cineclub_backend.cineclub_backend.shared.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a un usuario específico
     *
     * @param userId ID del usuario destinatario
     * @param destination Destino del mensaje (ej: "/queue/friend-requests")
     * @param payload Contenido de la notificación
     */
    public void sendNotificationToUser(String userId, String destination, Object payload) {
        try {
            String userDestination = "/user/" + userId + destination;
            messagingTemplate.convertAndSend(userDestination, payload);
            log.info("Notification sent to user {} at destination {}", userId, userDestination);
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Envía una notificación de solicitud de amistad
     *
     * @param userId ID del usuario destinatario
     * @param notification DTO con la información de la solicitud
     */
    public void sendFriendRequestNotification(String userId, Object notification) {
        sendNotificationToUser(userId, "/queue/friend-requests", notification);
    }

    /**
     * Envía una notificación de amistad aceptada
     *
     * @param userId ID del usuario destinatario
     * @param notification DTO con la información de la amistad aceptada
     */
    public void sendFriendRequestAcceptedNotification(String userId, Object notification) {
        sendNotificationToUser(userId, "/queue/friend-requests-accepted", notification);
    }
}
