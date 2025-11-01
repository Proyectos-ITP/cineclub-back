package com.cineclub_backend.cineclub_backend.movies.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "collections")
@Data
public class Collection {

    @Id
    private String id;

    @Field("movie_id")
    private List<String> movieId;

    @Field("user_id")
    private String userId;
}
