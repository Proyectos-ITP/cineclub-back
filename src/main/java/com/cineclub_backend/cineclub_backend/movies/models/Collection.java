package com.cineclub_backend.cineclub_backend.movies.models;

import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "collections")
@Data
public class Collection {

  @Id
  private String id;

  @Field("movies")
  private List<String> movies;

  @Field("user_id")
  private String userId;
}
