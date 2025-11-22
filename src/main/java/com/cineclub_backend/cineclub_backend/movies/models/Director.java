package com.cineclub_backend.cineclub_backend.movies.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "directors")
@Data
public class Director {

  @Id
  private String id;

  @Field("movie_id")
  private String movieId;

  private String director;
}
