package com.cineclub_backend.cineclub_backend.shared.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Cineclub Backend is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}