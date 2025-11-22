package com.cineclub_backend.cineclub_backend.movies.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
public class DirectorDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;

  private String director;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<MovieInfo> movies;

  @Data
  public static class MovieInfo {

    private String id;
    private String title;
  }
}
