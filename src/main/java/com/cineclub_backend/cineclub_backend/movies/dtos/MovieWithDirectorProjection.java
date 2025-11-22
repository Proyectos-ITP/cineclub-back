package com.cineclub_backend.cineclub_backend.movies.dtos;

import java.util.Date;
import lombok.Data;

@Data
public class MovieWithDirectorProjection {

  private String id;
  private int externalId;
  private String title;
  private String overview;
  private String genres;
  private Date releaseDate;
  private String posterPath;
  private int runtime;
  private String originalLanguage;
  private String director;
}
