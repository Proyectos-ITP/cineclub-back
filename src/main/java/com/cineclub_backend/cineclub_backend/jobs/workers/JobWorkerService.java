package com.cineclub_backend.cineclub_backend.jobs.workers;

import com.cineclub_backend.cineclub_backend.jobs.services.JobQueueService;
import com.cineclub_backend.cineclub_backend.shared.services.EmailService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class JobWorkerService {

  @Autowired
  private JobQueueService jobQueueService;

  @Autowired
  private EmailService emailService;

  @Async
  @Scheduled(fixedDelay = 3000)
  public void processJobs() {
    Map<String, Object> job;
    while ((job = jobQueueService.dequeueJob()) != null) {
      try {
        String type = (String) job.get("type");
        switch (type) {
          case "EMAIL_FRIEND_REJECTED" -> {
            String to = (String) job.get("to");
            String subject = (String) job.get("subject");
            String body = (String) job.get("body");

            emailService.sendEmail(to, subject, body);
            System.out.println("📨 Email enviado a " + to);
          }
          case "EMAIL_FRIEND_REQUEST" -> {
            String to = (String) job.get("to");
            String subject = (String) job.get("subject");
            String body = (String) job.get("body");

            emailService.sendEmail(to, subject, body);
            System.out.println("📨 Email enviado a " + to);
          }
          case "EMAIL_FRIEND_ACCEPTED" -> {
            String to = (String) job.get("to");
            String subject = (String) job.get("subject");
            String body = (String) job.get("body");

            emailService.sendEmail(to, subject, body);
            System.out.println("📨 Email enviado a " + to);
          }
          case "EMAIL_COLLECTION_REQUEST" -> {
            String to = (String) job.get("to");
            String subject = (String) job.get("subject");
            String body = (String) job.get("body");
            emailService.sendEmail(to, subject, body);
            System.out.println("📨 Email de colección enviado a " + to);
          }
          default -> System.out.println("⚠️ Tipo de job desconocido: " + type);
        }
      } catch (Exception e) {
        System.err.println("❌ Error procesando job: " + e.getMessage());
      }
    }
  }
}
