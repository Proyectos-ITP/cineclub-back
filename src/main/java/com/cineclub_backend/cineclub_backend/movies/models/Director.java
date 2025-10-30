package com.cineclub_backend.cineclub_backend.movies.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "directors")
@Data
public class Director {

    @Id
    private String id;

    @Field("movie_id")
    private String movieId;

    private String director;
}
