package com.cineclub_backend.cineclub_backend.social.repositories;

import com.cineclub_backend.cineclub_backend.users.models.User;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class Neo4jClient {

  private final Driver driver;

  public Neo4jClient(Driver driver) {
    this.driver = driver;
  }

  public void upsertUser(User user) {
    try (Session session = driver.session()) {
      session.executeWrite(tx -> {
        tx.run(
          "MERGE (u:User {userId: $userId}) " + "SET u.fullName = $fullName",
          Map.of("userId", user.getId(), "fullName", user.getFullName())
        );
        return null;
      });
    }
  }

  public void addFriendship(String userId1, String userId2) {
    try (Session session = driver.session()) {
      System.out.println("Adding friendship between " + userId1 + " and " + userId2);
      session.executeWrite(tx -> {
        tx.run(
          "MERGE (u1:User {userId: $userId1}) " +
            "MERGE (u2:User {userId: $userId2}) " +
            "MERGE (u1)-[:FRIEND]->(u2) " +
            "MERGE (u2)-[:FRIEND]->(u1)",
          Map.of("userId1", userId1, "userId2", userId2)
        );
        return null;
      });
    }
  }

  public void removeFriendship(String userId1, String userId2) {
    try (Session session = driver.session()) {
      System.out.println("Removing friendship between " + userId1 + " and " + userId2);
      session.executeWrite(tx -> {
        tx.run(
          "MATCH (u1:User {userId: $userId1}) " +
            "MATCH (u2:User {userId: $userId2}) " +
            "MATCH (u1)-[r1:FRIEND]->(u2) " +
            "MATCH (u2)-[r2:FRIEND]->(u1) " +
            "DELETE r1, r2",
          Map.of("userId1", userId1, "userId2", userId2)
        );
        return null;
      });
    }
  }

  public Page<String> getRecommendations(String userId, Pageable pageable) {
    try (Session session = driver.session()) {
      System.out.println("Getting recommendations for " + userId);
      return session.executeRead(tx -> {
        var countResult = tx.run(
          "MATCH (u:User {userId: $userId}) " +
            "MATCH (other:User) " +
            "WHERE other.userId <> $userId " +
            "AND NOT (u)-[:FRIEND]->(other) " +
            "RETURN COUNT(other) as total",
          Map.of("userId", userId)
        );
        long total = countResult.single().get("total").asLong();

        var dataResult = tx.run(
          "MATCH (u:User {userId: $userId}) " +
            "MATCH (other:User) " +
            "WHERE other.userId <> $userId " +
            "AND NOT (u)-[:FRIEND]->(other) " +
            "OPTIONAL MATCH (u)-[:FRIEND]->(common)-[:FRIEND]->(other) " +
            "RETURN other.userId AS userId, COUNT(common) AS mutualFriends " +
            "ORDER BY mutualFriends DESC " +
            "SKIP $skip LIMIT $limit",
          Map.of("userId", userId, "skip", pageable.getOffset(), "limit", pageable.getPageSize())
        );

        List<String> data = dataResult.list(r -> r.get("userId").asString());

        return new PageImpl<>(data, pageable, total);
      });
    }
  }

  public void clearDatabase() {
    try (Session session = driver.session()) {
      session.executeWrite(tx -> {
        tx.run("MATCH (n) DETACH DELETE n");
        return null;
      });
    }
  }
}
