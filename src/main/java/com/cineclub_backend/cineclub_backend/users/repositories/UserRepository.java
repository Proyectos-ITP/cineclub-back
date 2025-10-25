package com.cineclub_backend.cineclub_backend.users.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.cineclub_backend.cineclub_backend.users.models.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}
