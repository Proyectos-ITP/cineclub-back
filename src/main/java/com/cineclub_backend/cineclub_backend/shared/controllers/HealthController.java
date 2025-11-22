package com.cineclub_backend.cineclub_backend.shared.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  private final CacheManager cacheManager;

  public HealthController(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @GetMapping("/")
  public String home() {
    return "Cineclub Backend is running!";
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }

  @DeleteMapping("/cache/clear")
  public ResponseEntity<Map<String, Object>> clearCache() {
    Map<String, Object> response = new HashMap<>();
    int clearedCount = 0;
    int skippedCount = 0;

    for (String cacheName : cacheManager.getCacheNames()) {
      if ("openapi:spec".equals(cacheName)) {
        skippedCount++;
        continue;
      }

      var cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
        clearedCount++;
      }
    }

    response.put("status", "success");
    response.put("message", "Cache cleared successfully");
    response.put("cachesCleared", clearedCount);
    response.put("cachesSkipped", skippedCount);

    return ResponseEntity.ok(response);
  }
}
