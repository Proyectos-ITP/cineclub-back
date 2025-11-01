package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionResponseDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CreateCollectionItemDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindCollectionPagedDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudCollectionService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private final CrudCollectionService crudCollectionService;

    public CollectionController(CrudCollectionService crudCollectionService) {
        this.crudCollectionService = crudCollectionService;
    }

    @GetMapping
    public PagedResponseDto<CollectionResponseDto> getPagedCollections(
            @ParameterObject FindCollectionPagedDto findCollectionPagedDto,
            @AuthenticationPrincipal String userId) {
        PagedResponseDto<CollectionResponseDto> collections = crudCollectionService.getPagedCollections(
            findCollectionPagedDto.getTitle(),
            userId,
            findCollectionPagedDto.toPageable()
        );
        return collections;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionDto>> test (@RequestBody CreateCollectionItemDto body, @AuthenticationPrincipal String userId) {
        CollectionDto collection = crudCollectionService.createCollectionItem(body.getMovieId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("La película se agregó a tu biblioteca.", collection));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<String>> deleteCollectionItem(@PathVariable String movieId, @AuthenticationPrincipal String userId) {
        String rowId = crudCollectionService.deleteCollectionItem(movieId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("La película se eliminó de tu biblioteca.", rowId));
    }
}
