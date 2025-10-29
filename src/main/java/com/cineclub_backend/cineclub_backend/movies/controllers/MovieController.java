package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudMovieService;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final CrudMovieService crudMovieService;

    public MovieController(CrudMovieService crudMovieService) {
        this.crudMovieService = crudMovieService;
    }

    @GetMapping
    public PagedResponseDto<MovieDto> getAllMovies(@ParameterObject PaginationDto paginationDto) {
        Page<MovieDto> page = crudMovieService.getAllMovies(paginationDto.getTitle(), paginationDto.toPageable());
        return new PagedResponseDto<>(page);
    }

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable String id) {
        return crudMovieService.getMovieById(id);
    }

    @PostMapping
    public MovieDto createMovie(@Valid @RequestBody CreateMovieDto movie) {
        return crudMovieService.createMovie(movie);
    }

    @PatchMapping("/{id}")
    public MovieDto updateMovie(@PathVariable String id, @Valid @RequestBody UpdateMovieDto movie) {
        return crudMovieService.updateMovie(id, movie);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable String id) {
        crudMovieService.deleteMovie(id);
    }
}
