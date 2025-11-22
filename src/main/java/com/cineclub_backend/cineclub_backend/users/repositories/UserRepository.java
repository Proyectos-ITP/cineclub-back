package com.cineclub_backend.cineclub_backend.users.repositories;

import com.cineclub_backend.cineclub_backend.users.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
  User findByEmail(String email);
}
