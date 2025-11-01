package com.cineclub_backend.cineclub_backend.movies.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class CollectionDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    private List<String> movies;

    private String userId;
}