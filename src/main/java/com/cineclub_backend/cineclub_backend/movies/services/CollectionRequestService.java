package com.cineclub_backend.cineclub_backend.movies.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionRequestResponseDto;
import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import com.cineclub_backend.cineclub_backend.shared.templates.CollectionRequestTemplate;
import java.util.Optional;
import com.cineclub_backend.cineclub_backend.movies.models.CollectionRequest;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRepository;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRequestRepository;
import com.cineclub_backend.cineclub_backend.users.models.User;
import com.cineclub_backend.cineclub_backend.users.repositories.UserRepository;
import com.cineclub_backend.cineclub_backend.shared.services.WebSocketNotificationService;
import java.util.HashMap;
import java.util.Map;
import com.cineclub_backend.cineclub_backend.jobs.services.JobQueueService;

@Service
public class CollectionRequestService {

    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final JobQueueService jobQueueService;
    private final WebSocketNotificationService webSocketNotificationService;

    public CollectionRequestService(
            CollectionRequestRepository collectionRequestRepository,
            CollectionRepository collectionRepository,
            UserRepository userRepository,
            JobQueueService jobQueueService,
            WebSocketNotificationService webSocketNotificationService) {
        this.collectionRequestRepository = collectionRequestRepository;
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.jobQueueService = jobQueueService;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Transactional
    public void sendCollection(String senderId, String receiverId) {
        // Verificar si ya existe una solicitud pendiente
        if (collectionRequestRepository.findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "PENDING").isPresent()) {
            throw new RuntimeException("Ya has enviado una solicitud a este usuario.");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Usuario remitente no encontrado"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Usuario destinatario no encontrado"));

        CollectionRequest request = new CollectionRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        collectionRequestRepository.save(request);

        // Notificar por Email (vía Job Queue)
        String emailBody = CollectionRequestTemplate.collectionShared(
                sender.getFullName(),
                receiver.getFullName(),
                Optional.empty() // TODO: Agregar URL del frontend cuando esté lista
        );
        
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("type", "EMAIL_COLLECTION_REQUEST");
        jobData.put("to", receiver.getEmail());
        jobData.put("subject", "Te han compartido una colección de películas");
        jobData.put("body", emailBody);
        
        jobQueueService.enqueueJob(jobData);

        // Notificar por WebSocket
        CollectionRequestResponseDto notificationDto = toDto(request, sender);
        webSocketNotificationService.sendCollectionRequestNotification(receiverId, notificationDto);
    }

    public List<CollectionRequestResponseDto> getPendingRequests(String userId) {
        List<CollectionRequest> requests = collectionRequestRepository.findByReceiverIdAndStatus(userId, "PENDING");
        return requests.stream().map(request -> {
            User sender = userRepository.findById(request.getSenderId()).orElse(null);
            return toDto(request, sender);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void acceptRequest(String requestId, String userId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para aceptar esta solicitud");
        }
        
        if (!"PENDING".equals(request.getStatus())) {
             throw new RuntimeException("Esta solicitud ya ha sido procesada");
        }

        // Obtener colección del sender
        Collection senderCollection = collectionRepository.findByUserId(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("El remitente no tiene colección"));

        // Obtener o crear colección del receiver
        Collection receiverCollection = collectionRepository.findByUserId(userId)
                .orElse(new Collection());
        
        if (receiverCollection.getUserId() == null) {
            receiverCollection.setUserId(userId);
            receiverCollection.setMovies(new ArrayList<>());
        }

        // Fusionar películas
        List<String> receiverMovies = receiverCollection.getMovies();
        if (receiverMovies == null) {
            receiverMovies = new ArrayList<>();
        }
        
        if (senderCollection.getMovies() != null) {
            for (String movieId : senderCollection.getMovies()) {
                if (!receiverMovies.contains(movieId)) {
                    receiverMovies.add(movieId);
                }
            }
        }
        
        receiverCollection.setMovies(receiverMovies);
        collectionRepository.save(receiverCollection);

        // Actualizar estado de la solicitud
        request.setStatus("ACCEPTED");
        collectionRequestRepository.save(request);
    }

    @Transactional
    public void rejectRequest(String requestId, String userId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para rechazar esta solicitud");
        }

        collectionRequestRepository.delete(request);
    }

    private CollectionRequestResponseDto toDto(CollectionRequest request, User sender) {
        CollectionRequestResponseDto dto = new CollectionRequestResponseDto();
        dto.setId(request.getId());
        dto.setSenderId(request.getSenderId());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        if (sender != null) {
            dto.setSenderName(sender.getFullName());
            dto.setSenderEmail(sender.getEmail());
        }
        return dto;
    }
}
