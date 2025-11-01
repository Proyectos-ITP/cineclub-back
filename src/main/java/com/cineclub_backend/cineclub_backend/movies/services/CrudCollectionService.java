package com.cineclub_backend.cineclub_backend.movies.services;

import org.springframework.stereotype.Service;

import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRepository;

@Service
public class CrudCollectionService {
    private final CollectionRepository collectionRepository;
    
    public CrudCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }
    
    public Collection createCollection(Collection collection) {
        return collectionRepository.save(collection);
    }
    
}
 