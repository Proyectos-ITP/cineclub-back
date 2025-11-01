package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudCollectionService;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    // private final CrudCollectionService crudCollectionService;

    // public CollectionController(CrudCollectionService crudCollectionService) {
    //     this.crudCollectionService = crudCollectionService;
    // }
    
    @PostMapping
    public void test (@ParameterObject String movieId) {
        System.out.println(movieId);
    }
}
