package com.cineclub_backend.cineclub_backend.social.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "friendRequests")
@Data
public class FriendRequest {

    @Id
    private String id;

    @Field("sender_id")
    private String senderId;

    @Field("receiver_id")
    private String receiverId;

    private Date createdAt;

    @Field("status")
    private String status;
}
