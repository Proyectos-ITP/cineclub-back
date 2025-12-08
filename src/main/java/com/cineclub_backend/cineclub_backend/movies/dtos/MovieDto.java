package com.cineclub_backend.cineclub_backend.movies.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import lombok.Data;

@Data
public class MovieDto {

  private String id;
  private int externalId;
  private String title;
  private String overview;
  private String genres;
  private Date releaseDate;
  private String posterPath;
  private int runtime;
  private String originalLanguage;
  private Integer matchScore;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private int upVotes;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private int downVotes;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private double score;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String director;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String userVote;
}
