package com.cineclub_backend.cineclub_backend.social.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineclub_backend.cineclub_backend.social.models.Friend;
import com.cineclub_backend.cineclub_backend.social.models.FriendRequest;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendRequestRepository;
import com.cineclub_backend.cineclub_backend.social.repositories.FriendsRepository;

@Service
public class CrudFriendsRequestsService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendsRepository friendsRepository;

    public CrudFriendsRequestsService(FriendRequestRepository friendRequestRepository,
            FriendsRepository friendsRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendsRepository = friendsRepository;
    }

    public FriendRequest sendFriendRequest(String userId, String receiverId) {
        Optional<FriendRequest> existingRequest = friendRequestRepository.findBySenderIdAndReceiverId(userId,
                receiverId);

        if (existingRequest.isPresent()) {
            throw new RuntimeException("Ya existe una solicitud de amistad");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userId);
        friendRequest.setReceiverId(receiverId);
        friendRequest.setCreatedAt(new Date());
        friendRequest.setStatus("PENDING");

        return friendRequestRepository.save(friendRequest);
    }

    @Transactional
    public void acceptFriendRequest(String userId, String senderId) {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, userId)
                .orElseThrow(() -> new RuntimeException("No se encontró la solicitud de amistad"));

        if (!"PENDING".equals(friendRequest.getStatus())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        Date now = new Date();

        Friend friendship1 = new Friend();
        friendship1.setUserId(senderId);
        friendship1.setFriendId(userId);
        friendship1.setCreatedAt(now);

        Friend friendship2 = new Friend();
        friendship2.setUserId(userId);
        friendship2.setFriendId(senderId);
        friendship2.setCreatedAt(now);

        friendsRepository.save(friendship1);
        friendsRepository.save(friendship2);

        friendRequest.setStatus("ACCEPTED");
        friendRequestRepository.save(friendRequest);
    }

    public void rejectFriendRequest(String userId, String senderId) {
        FriendRequest friendRequest = friendRequestRepository.findBySenderIdAndReceiverId(senderId, userId)
                .orElseThrow(() -> new RuntimeException("No se encontró la solicitud de amistad"));

        if (!"PENDING".equals(friendRequest.getStatus())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        friendRequestRepository.delete(friendRequest);
    }

    public List<FriendRequest> getReceivedRequests(String userId) {
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverId(userId);
        return receivedRequests.stream()
                .filter(req -> "PENDING".equals(req.getStatus()))
                .collect(Collectors.toList());
    }

    public List<FriendRequest> getSentRequests(String userId) {
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderId(userId);

        return sentRequests.stream()
                .filter(req -> "PENDING".equals(req.getStatus()))
                .collect(Collectors.toList());
    }
}
