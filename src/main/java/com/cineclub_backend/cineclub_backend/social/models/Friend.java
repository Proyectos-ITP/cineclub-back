package com.cineclub_backend.cineclub_backend.social.models;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "friends")
@Data
public class Friend {

  @Id
  private String id;

  @Field("user_id")
  private String userId;

  @Field("friend_id")
  private String friendId;

  private Date createdAt;
}
