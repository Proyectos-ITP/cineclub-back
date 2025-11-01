package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CreateCollectionItemDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudCollectionService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final CrudCollectionService crudCollectionService;

    public CollectionController(CrudCollectionService crudCollectionService) {
        this.crudCollectionService = crudCollectionService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<CollectionDto>> test (@RequestBody CreateCollectionItemDto body, @AuthenticationPrincipal String userId) {
        CollectionDto collection = crudCollectionService.createCollectionItem(body.getMovieId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Pelicula agregada exitosamente.", collection));
    }
}
