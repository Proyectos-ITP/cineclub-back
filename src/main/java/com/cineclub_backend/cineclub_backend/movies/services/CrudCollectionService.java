package com.cineclub_backend.cineclub_backend.movies.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CreateCollectionItemDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CreateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import com.cineclub_backend.cineclub_backend.movies.models.Director;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRepository;

@Service
public class CrudCollectionService {
    private final CollectionRepository collectionRepository;
    
    public CrudCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }
    
    public CollectionDto createCollectionItem(String movie_id, String userId) {
        Collection collectionByUserExist = collectionRepository.findByUserId(userId).orElse(null);
        if (collectionByUserExist != null) {
            collectionByUserExist.getMovies().add(movie_id);
            Collection collection = collectionRepository.save(collectionByUserExist);
            return toDto(collection);
        }
        return toDto(collectionRepository.save(toEntity(movie_id, userId)));
    }
    
    public Collection toEntity(String movie_id, String userId) {
        if (movie_id == null || userId == null) {
            return null;
        }
        Collection collection = new Collection();
        collection.setMovies(List.of(movie_id));
        collection.setUserId(userId);
        return collection;
    }

    private CollectionDto toDto(Collection collection) {
        if (collection == null) {
            return null;
        }
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setMovies(collection.getMovies());
        dto.setUserId(collection.getUserId());
        return dto;
    }
}
 