package com.cineclub_backend.cineclub_backend.users.models;

import com.cineclub_backend.cineclub_backend.shared.helpers.ModelSerializables;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements ModelSerializables {

  @Id
  private String id;

  private String fullName;
  private String email;
  private String country;
  private String phone;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public User() {}

  public User(String id, String fullName, String email, String country, String phone) {
    this.id = id;
    this.fullName = fullName;
    this.email = email;
    this.country = country;
    this.phone = phone;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public String getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
