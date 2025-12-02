package com.cineclub_backend.cineclub_backend.movies.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class CollectionResponseDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;

  private List<MovieInfo> movies;

  private String userId;

  @Data
  public static class MovieInfo {

    private String id;
    private String title;
    private String posterPath;
    private String overview;
    private Date releaseDate;
  }
}
