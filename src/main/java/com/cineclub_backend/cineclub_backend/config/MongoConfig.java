package com.cineclub_backend.cineclub_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
  basePackages = {
    "com.cineclub_backend.cineclub_backend.movies.repositories",
    "com.cineclub_backend.cineclub_backend.users.repositories",
    "com.cineclub_backend.cineclub_backend.shared.repositories",
    "com.cineclub_backend.cineclub_backend.social.repositories",
    "com.cineclub_backend.cineclub_backend.reviews.repositories",
    "com.cineclub_backend.cineclub_backend.notifications.repositories",
  }
)
public class MongoConfig {}
