package com.cineclub_backend.cineclub_backend.movies.controllers;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudDirectorService;
import com.cineclub_backend.cineclub_backend.shared.dtos.ApiResponse;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/directors")
@Tag(name = "Directors", description = "Endpoints para gestionar directores")
public class DirectorController {

  private final CrudDirectorService crudDirectorService;

  public DirectorController(CrudDirectorService crudDirectorService) {
    this.crudDirectorService = crudDirectorService;
  }

  @GetMapping("/movies")
  @Operation(
    summary = "Listar directores con películas",
    description = "Obtiene la lista de directores con películas"
  )
  public PagedResponseDto<DirectorDto> getPagedDirectorsWithMovies(
    @ParameterObject FindDirectorDto findDirectorDto
  ) {
    Page<DirectorDto> page = crudDirectorService.getPagedDirectorsWithMovies(
      findDirectorDto.getDirector(),
      findDirectorDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }

  @GetMapping
  @Operation(summary = "Listar directores", description = "Obtiene la lista de directores")
  public PagedResponseDto<DirectorDto> getPagedDirectors(
    @ParameterObject FindDirectorDto findDirectorDto
  ) {
    Page<DirectorDto> page = crudDirectorService.getPagedDirectors(
      findDirectorDto.getDirector(),
      findDirectorDto.toPageable()
    );
    return new PagedResponseDto<>(page);
  }

  @GetMapping("/{id}")
  @Operation(
    summary = "Obtener director por ID",
    description = "Retorna la información de un director específico por su ID"
  )
  public ResponseEntity<ApiResponse<DirectorDto>> getDirectorById(@PathVariable String id) {
    DirectorDto director = crudDirectorService.getDirectorById(id);
    return ResponseEntity.ok(ApiResponse.success(director));
  }

  @PostMapping
  @Operation(summary = "Crear director", description = "Crea un nuevo director en el sistema")
  public ResponseEntity<ApiResponse<DirectorDto>> createDirector(
    @Valid @RequestBody CreateDirectorDto directorDto
  ) {
    DirectorDto director = crudDirectorService.createDirector(directorDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      ApiResponse.created("Director creado exitosamente", director)
    );
  }

  @PatchMapping("/{id}")
  @Operation(
    summary = "Actualizar director",
    description = "Actualiza la información de un director existente"
  )
  public ResponseEntity<ApiResponse<DirectorDto>> updateDirector(
    @PathVariable String id,
    @Valid @RequestBody UpdateDirectorDto directorDto
  ) {
    DirectorDto director = crudDirectorService.updateDirector(id, directorDto);
    return ResponseEntity.ok(ApiResponse.success("Director actualizado exitosamente", director));
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Eliminar director",
    description = "Elimina un director del sistema por su ID"
  )
  public ResponseEntity<ApiResponse<Void>> deleteDirector(@PathVariable String id) {
    crudDirectorService.deleteDirector(id);
    return ResponseEntity.ok(ApiResponse.success("Director eliminado exitosamente", null));
  }
}
