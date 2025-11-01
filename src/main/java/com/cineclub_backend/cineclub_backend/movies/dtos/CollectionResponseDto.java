package com.cineclub_backend.cineclub_backend.movies.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    }
}
