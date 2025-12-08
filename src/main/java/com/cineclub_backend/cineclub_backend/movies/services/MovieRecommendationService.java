// ===== FILTROS Y UTILIDADES =====
package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.models.MovieVote;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class MovieRecommendationService {

  private final MongoTemplate mongoTemplate;

  private static final int TOTAL_RECOMMENDATIONS = 20;
  private static final int QUOTA_GENRE = 10;
  private static final int QUOTA_DIRECTOR = 5;
  private static final int QUOTA_TEMPORAL = 5;
  private static final int QUOTA_SERENDIPITY = 5;

  public MovieRecommendationService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public List<MovieDto> run(String userId) {
    UserProfile profile = buildUserProfile(userId);

    if (profile.votedMovieIds.isEmpty()) {
      return getRandomRecommendations(25);
    }

    List<MovieDto> directorCandidates = getDirectorRecommendations(profile, 50);
    List<MovieDto> genreCandidates = getGenreRecommendations(profile, 100);
    List<MovieDto> temporalCandidates = getTemporalDiscovery(profile, 50);
    List<MovieDto> serendipityCandidates = getSerendipityRecommendations(profile, 50);

    List<MovieDto> finalSelection = selectWithGuaranteedQuotas(
      directorCandidates,
      genreCandidates,
      temporalCandidates,
      serendipityCandidates
    );

    Collections.shuffle(finalSelection);

    printRecommendationsSummary(finalSelection);

    return finalSelection;
  }

  private List<MovieDto> selectWithGuaranteedQuotas(
    List<MovieDto> directorCandidates,
    List<MovieDto> genreCandidates,
    List<MovieDto> temporalCandidates,
    List<MovieDto> serendipityCandidates
  ) {
    List<MovieDto> selected = new ArrayList<>();
    Set<String> selectedIds = new HashSet<>();

    int fromDirector = selectFromList(
      directorCandidates,
      QUOTA_DIRECTOR,
      selected,
      selectedIds,
      "Director"
    );
    int fromTemporal = selectFromList(
      temporalCandidates,
      QUOTA_TEMPORAL,
      selected,
      selectedIds,
      "Temporal"
    );
    int fromSerendipity = selectFromList(
      serendipityCandidates,
      QUOTA_SERENDIPITY,
      selected,
      selectedIds,
      "Serendipity"
    );

    int deficit =
      QUOTA_DIRECTOR -
      fromDirector +
      QUOTA_TEMPORAL -
      fromTemporal +
      QUOTA_SERENDIPITY -
      fromSerendipity;

    int genreQuota = QUOTA_GENRE + deficit;
    int fromGenre = selectFromList(genreCandidates, genreQuota, selected, selectedIds, "Género");

    if (selected.size() < TOTAL_RECOMMENDATIONS) {
      int remaining = TOTAL_RECOMMENDATIONS - selected.size();
      List<MovieDto> allRemaining = new ArrayList<>();
      allRemaining.addAll(directorCandidates);
      allRemaining.addAll(genreCandidates);
      allRemaining.addAll(temporalCandidates);
      allRemaining.addAll(serendipityCandidates);

      selectFromList(allRemaining, remaining, selected, selectedIds, "Mixto");
    }

    return selected;
  }

  private boolean passesQualityFilter(Document doc) {
    // Por ahora sin filtro de calidad (MIN_VOTES_THRESHOLD = 0)
    // Cambiar a >= 10 cuando haya suficientes votos en producción
    return true;
  }

  private int selectFromList(
    List<MovieDto> candidates,
    int quota,
    List<MovieDto> target,
    Set<String> selectedIds,
    String strategyName
  ) {
    int added = 0;
    for (MovieDto movie : candidates) {
      if (added >= quota) {
        break;
      }

      if (movie.getId() != null && !selectedIds.contains(movie.getId())) {
        target.add(movie);
        selectedIds.add(movie.getId());
        added++;
      }
    }

    return added;
  }

  private UserProfile buildUserProfile(String userId) {
    UserProfile profile = new UserProfile();

    Query voteQuery = new Query();
    voteQuery.addCriteria(Criteria.where("user_id").is(userId).and("type").is("UP"));
    List<MovieVote> upVotes = mongoTemplate.find(voteQuery, MovieVote.class, "movie_votes");

    profile.votedMovieIds = upVotes.stream().map(MovieVote::getMovieId).collect(Collectors.toSet());

    if (profile.votedMovieIds.isEmpty()) {
      return profile;
    }

    Query movieQuery = new Query(Criteria.where("_id").in(profile.votedMovieIds));
    List<Document> votedMovies = mongoTemplate.find(movieQuery, Document.class, "movies");

    Map<String, Integer> genreCounts = new HashMap<>();
    Map<Integer, Integer> yearCounts = new HashMap<>();
    Map<Integer, Integer> decadeCounts = new HashMap<>();

    for (Document movie : votedMovies) {
      extractGenres(movie.get("genres"), genreCounts);

      Integer year = extractYear(movie.get("release_date"));
      if (year != null) {
        yearCounts.put(year, yearCounts.getOrDefault(year, 0) + 1);
        int decade = (year / 10) * 10;
        decadeCounts.put(decade, decadeCounts.getOrDefault(decade, 0) + 1);
      }
    }

    Map<String, Integer> directorCounts = new HashMap<>();
    Query directorQuery = new Query(Criteria.where("movie_id").in(profile.votedMovieIds));
    List<Document> directorDocs = mongoTemplate.find(directorQuery, Document.class, "directors");

    for (Document doc : directorDocs) {
      String director = doc.getString("director");
      if (director != null && !director.trim().isEmpty()) {
        directorCounts.put(director, directorCounts.getOrDefault(director, 0) + 1);
      }
    }

    profile.topGenres = getTopKeys(genreCounts, 5);
    profile.topDirectors = getTopKeys(directorCounts, 10);
    profile.favoriteYears = getTopKeys(yearCounts, 10);
    profile.favoriteDecades = getTopKeys(decadeCounts, 3);
    profile.genreCounts = genreCounts;
    profile.directorCounts = directorCounts;

    return profile;
  }

  private List<MovieDto> getDirectorRecommendations(UserProfile profile, int quota) {
    if (profile.topDirectors.isEmpty()) {
      return new ArrayList<>();
    }

    Query dirQuery = new Query();
    dirQuery.addCriteria(
      Criteria.where("director").in(profile.topDirectors).and("movie_id").nin(profile.votedMovieIds)
    );
    dirQuery.limit(100);

    List<Document> directorDocs = mongoTemplate.find(dirQuery, Document.class, "directors");

    if (directorDocs.isEmpty()) {
      return new ArrayList<>();
    }

    List<String> movieIds = directorDocs
      .stream()
      .map(d -> d.getString("movie_id"))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    List<org.bson.types.ObjectId> objectIds = new ArrayList<>();
    for (String id : movieIds) {
      try {
        objectIds.add(new org.bson.types.ObjectId(id));
      } catch (Exception e) {
        System.out.println("No se pudo convertir ID: " + id);
      }
    }

    Query movieQuery = new Query(Criteria.where("_id").in(objectIds));
    List<Document> candidates = mongoTemplate.find(movieQuery, Document.class, "movies");

    if (candidates.isEmpty() && !movieIds.isEmpty()) {
      movieQuery = new Query(Criteria.where("_id").in(movieIds));
      candidates = mongoTemplate.find(movieQuery, Document.class, "movies");
    }

    Map<String, String> movieToDirector = new HashMap<>();
    for (Document dirDoc : directorDocs) {
      movieToDirector.put(dirDoc.getString("movie_id"), dirDoc.getString("director"));
    }

    if (candidates.isEmpty()) {
      return new ArrayList<>();
    }

    if (!candidates.isEmpty()) {
      Document first = candidates.get(0);
    }

    List<ScoredMovie> scored = new ArrayList<>();
    int filtered = 0;
    int withoutDirector = 0;

    for (Document doc : candidates) {
      String movieId = getMovieId(doc);
      String director = movieToDirector.get(movieId);

      if (director == null) {
        withoutDirector++;
        continue;
      }

      boolean passes = passesQualityFilter(doc);

      if (passes) {
        double score = profile.directorCounts.getOrDefault(director, 0) * 10.0;
        score += Math.log(1 + getInt(doc, "up_votes")) * 2;
        scored.add(new ScoredMovie(doc, score, director));
      } else {
        filtered++;
      }
    }

    scored.sort((a, b) -> Double.compare(b.score, a.score));

    List<MovieDto> result = scored
      .stream()
      .limit(quota)
      .map(sm -> {
        MovieDto dto = convertToDto(sm.document);
        dto.setDirector(sm.director);
        return dto;
      })
      .collect(Collectors.toList());
    return result;
  }

  private List<MovieDto> getGenreRecommendations(UserProfile profile, int limit) {
    if (profile.topGenres.isEmpty()) {
      return new ArrayList<>();
    }

    List<String> mainGenres = profile.topGenres.stream().limit(3).collect(Collectors.toList());
    String regexPattern = String.join("|", mainGenres);
    List<Document> pipeline = new ArrayList<>();
    Document matchStage = new Document(
      "$match",
      new Document()
        .append("genres", new Document("$regex", regexPattern).append("$options", "i"))
        .append("_id", new Document("$nin", new ArrayList<>(profile.votedMovieIds)))
    );
    pipeline.add(matchStage);

    Document sampleStage = new Document("$sample", new Document("size", 300));
    pipeline.add(sampleStage);

    List<Document> candidates = mongoTemplate
      .getCollection("movies")
      .aggregate(pipeline)
      .into(new ArrayList<>());

    Map<String, String> directors = getDirectorsForMovies(candidates);
    List<ScoredMovie> scored = new ArrayList<>();

    for (Document doc : candidates) {
      if (!passesQualityFilter(doc)) {
        continue;
      }

      double score = 0;

      Set<String> movieGenres = extractGenresFromString(doc.get("genres"));
      for (String genre : movieGenres) {
        score += profile.genreCounts.getOrDefault(genre, 0) * 3.0;
      }

      score += Math.log(1 + getInt(doc, "up_votes") + 1) * 2;

      String movieId = getMovieId(doc);
      String director = directors.get(movieId);
      if (director != null && profile.topDirectors.contains(director)) {
        score += 5.0;
      }

      Integer year = extractYear(doc.get("release_date"));
      if (year != null) {
        int decade = (year / 10) * 10;
        if (profile.favoriteDecades.contains(decade)) {
          score += 3.0;
        }
      }

      scored.add(new ScoredMovie(doc, score, director));
    }

    scored.sort((a, b) -> Double.compare(b.score, a.score));

    List<MovieDto> result = scored
      .stream()
      .limit(limit)
      .map(sm -> {
        MovieDto dto = convertToDto(sm.document);
        dto.setDirector(sm.director);
        return dto;
      })
      .collect(Collectors.toList());
    return result;
  }

  private List<MovieDto> getTemporalDiscovery(UserProfile profile, int limit) {
    List<Document> pipeline = new ArrayList<>();
    Document matchStage = new Document(
      "$match",
      new Document("_id", new Document("$nin", new ArrayList<>(profile.votedMovieIds)))
    );
    pipeline.add(matchStage);

    Document sampleStage = new Document("$sample", new Document("size", 300));
    pipeline.add(sampleStage);

    List<Document> candidates = mongoTemplate
      .getCollection("movies")
      .aggregate(pipeline)
      .into(new ArrayList<>());

    Map<String, String> directors = getDirectorsForMovies(candidates);
    List<ScoredMovie> scored = new ArrayList<>();
    Map<Integer, Integer> decadeCount = new HashMap<>();

    for (Document doc : candidates) {
      if (!passesQualityFilter(doc)) {
        continue;
      }

      double score = 0;

      Integer year = extractYear(doc.get("release_date"));
      if (year != null) {
        int decade = (year / 10) * 10;

        if (profile.favoriteDecades.contains(decade)) {
          score -= 10.0;
        } else {
          score += 15.0;
        }

        int count = decadeCount.getOrDefault(decade, 0);
        score -= count * 2.0;
        decadeCount.put(decade, count + 1);
      }

      score += Math.log(1 + getInt(doc, "up_votes") + 1) * 3;

      Set<String> movieGenres = extractGenresFromString(doc.get("genres"));
      for (String genre : movieGenres) {
        if (profile.topGenres.contains(genre)) {
          score += 1.0;
        }
      }

      String movieId = getMovieId(doc);
      scored.add(new ScoredMovie(doc, score, directors.get(movieId)));
    }

    scored.sort((a, b) -> Double.compare(b.score, a.score));

    List<MovieDto> result = scored
      .stream()
      .limit(limit)
      .map(sm -> {
        MovieDto dto = convertToDto(sm.document);
        dto.setDirector(sm.director);
        return dto;
      })
      .collect(Collectors.toList());

    return result;
  }

  private List<MovieDto> getSerendipityRecommendations(UserProfile profile, int limit) {
    List<Document> pipeline = new ArrayList<>();
    Document matchStage = new Document(
      "$match",
      new Document("_id", new Document("$nin", new ArrayList<>(profile.votedMovieIds)))
    );
    pipeline.add(matchStage);

    Document sampleStage = new Document("$sample", new Document("size", 200));
    pipeline.add(sampleStage);

    List<Document> candidates = mongoTemplate
      .getCollection("movies")
      .aggregate(pipeline)
      .into(new ArrayList<>());

    Map<String, String> directors = getDirectorsForMovies(candidates);
    List<ScoredMovie> scored = new ArrayList<>();

    for (Document doc : candidates) {
      if (!passesQualityFilter(doc)) {
        continue;
      }

      Set<String> movieGenres = extractGenresFromString(doc.get("genres"));
      long genreMatches = movieGenres.stream().filter(profile.topGenres::contains).count();
      String movieId = getMovieId(doc);
      String director = directors.get(movieId);
      boolean isKnownDirector = director != null && profile.topDirectors.contains(director);

      if (genreMatches <= 1 && !isKnownDirector) {
        double score = Math.log(1 + getInt(doc, "up_votes") + 1) * 10;
        scored.add(new ScoredMovie(doc, score, director));
      }
    }

    scored.sort((a, b) -> Double.compare(b.score, a.score));

    List<MovieDto> result = scored
      .stream()
      .limit(limit)
      .map(sm -> {
        MovieDto dto = convertToDto(sm.document);
        dto.setDirector(sm.director);
        return dto;
      })
      .collect(Collectors.toList());

    return result;
  }

  private List<MovieDto> getRandomRecommendations(int limit) {
    List<Document> pipeline = new ArrayList<>();

    pipeline.add(new Document("$sample", new Document("size", limit)));

    List<Document> candidates = mongoTemplate
      .getCollection("movies")
      .aggregate(pipeline)
      .into(new ArrayList<>());

    Map<String, String> directors = getDirectorsForMovies(candidates);

    return candidates
      .stream()
      .map(doc -> {
        MovieDto dto = convertToDto(doc);
        String movieId = getMovieId(doc);
        String director = directors.get(movieId);
        dto.setDirector(director);
        return dto;
      })
      .collect(Collectors.toList());
  }

  private void printRecommendationsSummary(List<MovieDto> movies) {
    if (movies.isEmpty()) {
      return;
    }

    Map<Integer, Long> byDecade = movies
      .stream()
      .map(m -> extractYear(m.getReleaseDate()))
      .filter(Objects::nonNull)
      .collect(Collectors.groupingBy(year -> (year / 10) * 10, Collectors.counting()));

    byDecade
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .forEach(e -> System.out.println("   " + e.getKey() + "s: " + e.getValue() + " películas"));

    long withKnownDirectors = movies
      .stream()
      .filter(m -> m.getDirector() != null && !m.getDirector().isEmpty())
      .count();
  }

  private static class UserProfile {

    Set<String> votedMovieIds = new HashSet<>();
    List<String> topGenres = new ArrayList<>();
    List<String> topDirectors = new ArrayList<>();
    List<Integer> favoriteYears = new ArrayList<>();
    List<Integer> favoriteDecades = new ArrayList<>();
    Map<String, Integer> genreCounts = new HashMap<>();
    Map<String, Integer> directorCounts = new HashMap<>();
  }

  private static class ScoredMovie {

    Document document;
    double score;
    String director;

    ScoredMovie(Document document, double score, String director) {
      this.document = document;
      this.score = score;
      this.director = director;
    }
  }

  private <K> List<K> getTopKeys(Map<K, Integer> map, int limit) {
    return map
      .entrySet()
      .stream()
      .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
      .limit(limit)
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
  }

  private String getMovieId(Document doc) {
    Object idObj = doc.get("_id");
    if (idObj instanceof org.bson.types.ObjectId) {
      return ((org.bson.types.ObjectId) idObj).toString();
    }
    return idObj != null ? idObj.toString() : null;
  }

  private Integer extractYear(Object releaseDateObj) {
    if (releaseDateObj instanceof Date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) releaseDateObj);
      return cal.get(Calendar.YEAR);
    } else if (releaseDateObj instanceof String) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse((String) releaseDateObj);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  private Map<String, String> getDirectorsForMovies(List<Document> movies) {
    Map<String, String> map = new HashMap<>();
    List<String> ids = movies.stream().map(this::getMovieId).collect(Collectors.toList());
    if (ids.isEmpty()) {
      return map;
    }

    Query query = new Query(Criteria.where("movie_id").in(ids));
    List<Document> dirs = mongoTemplate.find(query, Document.class, "directors");

    for (Document d : dirs) {
      map.put(d.getString("movie_id"), d.getString("director"));
    }
    return map;
  }

  private void extractGenres(Object genresObj, Map<String, Integer> counts) {
    if (genresObj == null) {
      return;
    }
    if (genresObj instanceof String) {
      String genresStr = (String) genresObj;
      Set<String> found = new HashSet<>();
      Pattern pattern1 = Pattern.compile("'name':\\s*'([^']+)'");
      Matcher matcher1 = pattern1.matcher(genresStr);
      while (matcher1.find()) {
        found.add(matcher1.group(1));
      }
      if (found.isEmpty()) {
        Pattern pattern2 = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");
        Matcher matcher2 = pattern2.matcher(genresStr);
        while (matcher2.find()) {
          found.add(matcher2.group(1));
        }
      }
      for (String g : found) {
        counts.put(g, counts.getOrDefault(g, 0) + 1);
      }
    }
  }

  private Set<String> extractGenresFromString(Object genresObj) {
    Set<String> genres = new HashSet<>();
    if (genresObj == null) {
      return genres;
    }
    if (genresObj instanceof String) {
      String genresStr = (String) genresObj;
      Pattern pattern = Pattern.compile("'name':\\s*'([^']+)'");
      Matcher matcher = pattern.matcher(genresStr);
      while (matcher.find()) {
        genres.add(matcher.group(1));
      }
      if (genres.isEmpty()) {
        Pattern p2 = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");
        Matcher m2 = p2.matcher(genresStr);
        while (m2.find()) {
          genres.add(m2.group(1));
        }
      }
    } else if (genresObj instanceof List) {
      List<?> list = (List<?>) genresObj;
      for (Object item : list) {
        if (item instanceof String) {
          genres.add((String) item);
        } else if (item instanceof Document) {
          Document d = (Document) item;
          if (d.containsKey("name")) {
            genres.add(d.getString("name"));
          }
        } else if (item instanceof Map) {
          Map<?, ?> m = (Map<?, ?>) item;
          if (m.containsKey("name")) {
            genres.add(m.get("name").toString());
          }
        }
      }
    }
    return genres;
  }

  private MovieDto convertToDto(Document doc) {
    MovieDto dto = new MovieDto();
    try {
      dto.setId(getMovieId(doc));
      dto.setTitle(doc.getString("title"));
      dto.setOverview(doc.getString("overview"));
      Object genres = doc.get("genres");
      if (genres instanceof String) {
        dto.setGenres((String) genres);
      } else if (genres != null) {
        dto.setGenres(genres.toString());
      }
      dto.setReleaseDate(doc.getDate("release_date"));
      dto.setPosterPath(doc.getString("poster_path"));
      dto.setOriginalLanguage(doc.getString("original_language"));
      dto.setRuntime(getInt(doc, "runtime"));
      dto.setUpVotes(getInt(doc, "up_votes"));
      dto.setDownVotes(getInt(doc, "down_votes"));
    } catch (Exception e) {
      System.out.println("Error convirtiendo documento: " + e.getMessage());
    }
    return dto;
  }

  private Integer getInt(Document doc, String key) {
    Object val = doc.get(key);
    if (val instanceof Number) {
      return ((Number) val).intValue();
    }
    return 0;
  }
}
