package com.cineclub_backend.cineclub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineclubBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(CineclubBackendApplication.class, args);
  }
}
