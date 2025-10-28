package com.cineclub_backend.cineclub_backend.movies.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.models.Movie;
import com.cineclub_backend.cineclub_backend.movies.repositories.MovieRepository;

import com.cineclub_backend.cineclub_backend.shared.exceptions.ResourceNotFoundException;

@Service
public class CrudMovieService {

    private final MovieRepository movieRepository;

    public CrudMovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Page<MovieDto> getAllMovies(String title, Pageable pageable) {
        if (title != null && !title.isEmpty()) {
            return movieRepository.findByTitleContainingIgnoreCase(title, pageable).map(this::toDto);
        }
        return movieRepository.findAll(pageable).map(this::toDto);
    }

    public MovieDto getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Película no encontrada con el id: " + id));
        return toDto(movie);
    }

    public MovieDto createMovie(CreateMovieDto movieDto) {
        Movie movie = toEntity(movieDto);
        if (movie.getGenres() == null || movie.getGenres().isEmpty()) {
            movie.setGenres("Uncategorized");
        }
        movie = movieRepository.save(movie);
        return toDto(movie);
    }

    public MovieDto updateMovie(String id, UpdateMovieDto movieDto) {
        return movieRepository.findById(id).map(movie -> {
            if (movieDto.getExternalId() != 0) {
                movie.setExternalId(movieDto.getExternalId());
            }
            if (movieDto.getTitle() != null) {
                movie.setTitle(movieDto.getTitle());
            }
            if (movieDto.getOverview() != null) {
                movie.setOverview(movieDto.getOverview());
            }
            if (movieDto.getGenres() != null) {
                movie.setGenres(movieDto.getGenres());
            }
            if (movieDto.getReleaseDate() != null) {
                movie.setReleaseDate(movieDto.getReleaseDate());
            }
            if (movieDto.getPosterPath() != null) {
                movie.setPosterPath(movieDto.getPosterPath());
            }
            if (movieDto.getRuntime() != 0) {
                movie.setRuntime(movieDto.getRuntime());
            }
            if (movieDto.getOriginalLanguage() != null) {
                movie.setOriginalLanguage(movieDto.getOriginalLanguage());
            }
            Movie updatedMovie = movieRepository.save(movie);
            return toDto(updatedMovie);
        }).orElseThrow(() -> new ResourceNotFoundException(
                "Película no encontrada con el id: " + id));
    }

    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Película no encontrada con el id: " + id);
        }
        movieRepository.deleteById(id);
    }

    private MovieDto toDto(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieDto dto = new MovieDto();
        dto.setId(movie.getId());
        dto.setExternalId(movie.getExternalId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setGenres(movie.getGenres());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterPath(movie.getPosterPath());
        dto.setRuntime(movie.getRuntime());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        return dto;
    }

    private Movie toEntity(CreateMovieDto dto) {
        if (dto == null) {
            return null;
        }
        Movie movie = new Movie();
        movie.setExternalId(dto.getExternalId());
        movie.setTitle(dto.getTitle());
        movie.setOverview(dto.getOverview());
        movie.setGenres(dto.getGenres());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPosterPath(dto.getPosterPath());
        movie.setRuntime(dto.getRuntime());
        movie.setOriginalLanguage(dto.getOriginalLanguage());
        return movie;
    }

}
