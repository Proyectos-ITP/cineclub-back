package com.cineclub_backend.cineclub_backend.movies.controllers;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.VoteMovieDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudMovieService;
import com.cineclub_backend.cineclub_backend.movies.services.CrudMovieVoteService;
import com.cineclub_backend.cineclub_backend.movies.services.MovieRecommendationService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
@Tag(name = "Movies", description = "Endpoints para gestionar películas")
public class MovieController {

  private final CrudMovieService crudMovieService;
  private final CrudMovieVoteService crudMovieVoteService;
  private final MovieRecommendationService movieRecommendationService;

  public MovieController(
    CrudMovieService crudMovieService,
    CrudMovieVoteService crudMovieVoteService,
    MovieRecommendationService movieRecommendationService
  ) {
    this.crudMovieService = crudMovieService;
    this.crudMovieVoteService = crudMovieVoteService;
    this.movieRecommendationService = movieRecommendationService;
  }

  @GetMapping
  @Operation(summary = "Listar películas", description = "Obtiene la lista de películas")
  public PagedResponseDto<MovieDto> getAllMovies(
    @ParameterObject FindMovieDto findMovieDto,
    @AuthenticationPrincipal String userId
  ) {
    Page<MovieDto> page = crudMovieService.getAllMovies(
      findMovieDto.getTitle(),
      findMovieDto.toPageable(),
      userId
    );
    return new PagedResponseDto<>(page);
  }

  @GetMapping("/{id}")
  @Operation(
    summary = "Obtener película por ID",
    description = "Retorna la información de una película específica por su ID"
  )
  public ResponseEntity<ApiResponse<MovieDto>> getMovieById(@PathVariable String id) {
    MovieDto movie = crudMovieService.getMovieById(id);
    return ResponseEntity.ok(ApiResponse.success(movie));
  }

  @PostMapping
  @Operation(summary = "Crear película", description = "Crea una nueva película en el sistema")
  public ResponseEntity<ApiResponse<MovieDto>> createMovie(
    @Valid @RequestBody CreateMovieDto movie
  ) {
    MovieDto createdMovie = crudMovieService.createMovie(movie);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("Pelicula creada exitosamente", createdMovie)
    );
  }

  @PatchMapping("/{id}")
  @Operation(
    summary = "Actualizar película",
    description = "Actualiza la información de una película existente"
  )
  public ResponseEntity<ApiResponse<MovieDto>> updateMovie(
    @PathVariable String id,
    @Valid @RequestBody UpdateMovieDto movie
  ) {
    MovieDto updatedMovie = crudMovieService.updateMovie(id, movie);
    return ResponseEntity.ok(
      ApiResponse.success("Pelicula actualizada exitosamente", updatedMovie)
    );
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Eliminar película",
    description = "Elimina una película del sistema por su ID"
  )
  public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String id) {
    crudMovieService.deleteMovie(id);
    return ResponseEntity.ok(ApiResponse.success("Pelicula eliminada exitosamente", null));
  }

  @PostMapping("/{id}/vote")
  @Operation(
    summary = "Votar película",
    description = "Permite votar una película (UP/DOWN). Si ya votó lo mismo, se quita el voto."
  )
  public ResponseEntity<ApiResponse<Void>> voteMovie(
    @PathVariable String id,
    @Valid @RequestBody VoteMovieDto voteDto,
    @AuthenticationPrincipal String userId
  ) {
    crudMovieVoteService.voteMovie(id, userId, voteDto.getType());
    return ResponseEntity.ok(ApiResponse.success("Voto registrado exitosamente", null));
  }

  @GetMapping("/top")
  @Operation(
    summary = "Top películas",
    description = "Obtiene el top de películas basado en votos (upVotes - downVotes)"
  )
  public ResponseEntity<ApiResponse<List<MovieDto>>> getTopMovies(
    @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
    @AuthenticationPrincipal String userId
  ) {
    List<MovieDto> movies = crudMovieService.getTopMovies(limit, userId);
    return ResponseEntity.ok(ApiResponse.success(movies));
  }

  @GetMapping("/recommended")
  @Operation(
    summary = "Recomendaciones de películas",
    description = "Obtiene 20 películas aleatorias basadas en los votos UP del usuario (coincidencia de géneros y año)"
  )
  public ResponseEntity<ApiResponse<List<MovieDto>>> getRecommendedMovies(
    @AuthenticationPrincipal String userId
  ) {
    List<MovieDto> movies = movieRecommendationService.run(userId);
    return ResponseEntity.ok(ApiResponse.success(movies));
  }

  @GetMapping("/random")
  @Operation(
    summary = "Película aleatoria",
    description = "Obtiene una película aleatoria de la base de datos"
  )
  public ResponseEntity<ApiResponse<MovieDto>> getRandomMovie() {
    MovieDto movie = crudMovieService.getRandomMovie();
    return ResponseEntity.ok(ApiResponse.success(movie));
  }
}
