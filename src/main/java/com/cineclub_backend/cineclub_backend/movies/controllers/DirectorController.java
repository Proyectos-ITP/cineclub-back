package com.cineclub_backend.cineclub_backend.movies.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.FindDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudDirectorService;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    
    private final CrudDirectorService crudDirectorService;

    public DirectorController(CrudDirectorService crudDirectorService) {
        this.crudDirectorService = crudDirectorService;
    }

    @GetMapping("/movies")
    public PagedResponseDto<DirectorDto> getPagedDirectorsWithMovies(@ParameterObject FindDirectorDto findDirectorDto) {
        Page<DirectorDto> page = crudDirectorService.getPagedDirectorsWithMovies(findDirectorDto.getDirector(), findDirectorDto.toPageable());
        return new PagedResponseDto<>(page);
    }

    @GetMapping
    public PagedResponseDto<DirectorDto> getPagedDirectors(@ParameterObject FindDirectorDto findDirectorDto) {
        Page<DirectorDto> page = crudDirectorService.getPagedDirectors(findDirectorDto.getDirector(), findDirectorDto.toPageable());
        return new PagedResponseDto<>(page);
    }

    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable String id) {
        return crudDirectorService.getDirectorById(id);
    }

    @PostMapping
    public DirectorDto createDirector(@Valid @RequestBody CreateDirectorDto directorDto) {
        return crudDirectorService.createDirector(directorDto);
    }

    @PatchMapping("/{id}")
    public DirectorDto updateDirector(@PathVariable String id, @Valid @RequestBody UpdateDirectorDto directorDto) {
        return crudDirectorService.updateDirector(id, directorDto);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable String id) {
        crudDirectorService.deleteDirector(id);
    }
}
