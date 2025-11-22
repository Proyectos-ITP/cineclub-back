package com.cineclub_backend.cineclub_backend.jobs.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobQueueService {

  private static final String JOB_QUEUE = "job_queue";

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void enqueueJob(Map<String, Object> jobData) {
    try {
      jobData.put("enqueuedAt", LocalDateTime.now().toString());
      String json = objectMapper.writeValueAsString(jobData);
      redisTemplate.opsForList().rightPush(JOB_QUEUE, json);
      System.out.println("📦 Job agregado a la cola: " + jobData.get("type"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Map<String, Object> dequeueJob() {
    try {
      String json = redisTemplate.opsForList().leftPop(JOB_QUEUE);
      if (json == null) {
        return null;
      }
      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
