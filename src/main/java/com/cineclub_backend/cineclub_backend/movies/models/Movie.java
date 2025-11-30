package com.cineclub_backend.cineclub_backend.movies.models;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "movies")
@Data
public class Movie {

  @Id
  private String id;

  @Field("external_id")
  private int externalId;

  private String title;
  private String overview;
  private String genres;

  @Field("release_date")
  private Date releaseDate;

  @Field("poster_path")
  private String posterPath;

  private int runtime;

  @Field("original_language")
  private String originalLanguage;

  @Field("up_votes")
  private int upVotes;

  @Field("down_votes")
  private int downVotes;
}
