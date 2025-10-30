package com.cineclub_backend.cineclub_backend.movies.dtos;

import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String director;
}
