package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudMovieService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movies", description = "Endpoints para gestionar películas")
public class MovieController {

    private final CrudMovieService crudMovieService;

    public MovieController(CrudMovieService crudMovieService) {
        this.crudMovieService = crudMovieService;
    }

    @GetMapping
    @Operation(summary = "Listar películas", description = "Obtiene la lista de películas")
    public PagedResponseDto<MovieDto> getAllMovies(@ParameterObject FindMovieDto findMovieDto) {
        Page<MovieDto> page = crudMovieService.getAllMovies(findMovieDto.getTitle(), findMovieDto.toPageable());
        return new PagedResponseDto<>(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener película por ID", description = "Retorna la información de una película específica por su ID")
    public ResponseEntity<ApiResponse<MovieDto>> getMovieById(@PathVariable String id) {
        MovieDto movie = crudMovieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @PostMapping
    @Operation(summary = "Crear película", description = "Crea una nueva película en el sistema")
    public ResponseEntity<ApiResponse<MovieDto>> createMovie(@Valid @RequestBody CreateMovieDto movie) {
        MovieDto createdMovie = crudMovieService.createMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Pelicula creada exitosamente", createdMovie));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar película", description = "Actualiza la información de una película existente")
    public ResponseEntity<ApiResponse<MovieDto>> updateMovie(@PathVariable String id, @Valid @RequestBody UpdateMovieDto movie) {
        MovieDto updatedMovie = crudMovieService.updateMovie(id, movie);
        return ResponseEntity.ok(ApiResponse.success("Pelicula actualizada exitosamente", updatedMovie));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar película", description = "Elimina una película del sistema por su ID")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String id) {
        crudMovieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Pelicula eliminada exitosamente", null));
    }
}
