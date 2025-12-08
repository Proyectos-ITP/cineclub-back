package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CreateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateMovieDto;
import com.cineclub_backend.cineclub_backend.movies.models.Movie;
import com.cineclub_backend.cineclub_backend.movies.repositories.MovieRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrudMovieService {

  private final MovieRepository movieRepository;
  private final CrudDirectorService crudDirectorService;
  private final MongoTemplate mongoTemplate;

  public CrudMovieService(
    MovieRepository movieRepository,
    CrudDirectorService crudDirectorService,
    MongoTemplate mongoTemplate
  ) {
    this.movieRepository = movieRepository;
    this.crudDirectorService = crudDirectorService;
    this.mongoTemplate = mongoTemplate;
  }

  public Page<MovieDto> getAllMovies(String title, Pageable pageable, String userId) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      if (title != null && !title.isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("title").regex(title, "i")));
      }

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort().and(Sort.by(Direction.DESC, "_id"))),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize()),
          Aggregation.stage(
            "{ $lookup: { " +
              "  from: 'directors', " +
              "  let: { movie_id_str: { $toString: '$_id' } }, " +
              "  pipeline: [ " +
              "    { $match: { $expr: { $eq: ['$movie_id', '$$movie_id_str'] } } } " +
              "  ], " +
              "  as: 'director' " +
              "} }"
          ),
          Aggregation.unwind("director", true),
          Aggregation.stage(
            "{ $lookup: { " +
              "  from: 'movie_votes', " +
              "  let: { movie_id_str: { $toString: '$_id' } }, " +
              "  pipeline: [ " +
              "    { $match: { $expr: { $and: [ { $eq: ['$movie_id', '$$movie_id_str'] }, { $eq: ['$user_id', '" +
              userId +
              "'] } ] } } } " +
              "  ], " +
              "  as: 'vote' " +
              "} }"
          ),
          Aggregation.project()
            .and("_id")
            .as("id")
            .and("external_id")
            .as("externalId")
            .and("title")
            .as("title")
            .and("overview")
            .as("overview")
            .and("genres")
            .as("genres")
            .and("release_date")
            .as("releaseDate")
            .and("poster_path")
            .as("posterPath")
            .and("runtime")
            .as("runtime")
            .and("original_language")
            .as("originalLanguage")
            .and("director.director")
            .as("director")
            .and("vote")
            .as("vote")
            .and("up_votes")
            .as("upVotes")
            .and("down_votes")
            .as("downVotes")
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);

      AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
        aggregation,
        "movies",
        Document.class
      );

      Document result = aggregationResults.getUniqueMappedResult();

      if (result == null) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
      }

      @SuppressWarnings("unchecked")
      List<Document> metadata = (List<Document>) result.get("metadata");
      long total = metadata.isEmpty() ? 0 : metadata.get(0).getInteger("total", 0);

      @SuppressWarnings("unchecked")
      List<Document> data = (List<Document>) result.get("data");
      List<MovieDto> movieDtos = data.stream().map(this::convertDocumentToMovieDto).toList();

      return new PageImpl<>(movieDtos, pageable, total);
    } catch (Exception e) {
      throw e;
    }
  }

  @SuppressWarnings("unchecked")
  private MovieDto convertDocumentToMovieDto(Document doc) {
    MovieDto dto = new MovieDto();

    Object idObj = doc.get("id");
    if (idObj != null) {
      dto.setId(idObj.toString());
    }

    Object externalIdObj = doc.get("externalId");
    if (externalIdObj instanceof Number) {
      dto.setExternalId(((Number) externalIdObj).intValue());
    }

    dto.setTitle(doc.getString("title"));
    dto.setOverview(doc.getString("overview"));
    Object genresObj = doc.get("genres");
    if (genresObj instanceof List) {
      dto.setGenres(String.join(", ", (List<String>) genresObj));
    } else if (genresObj instanceof String) {
      dto.setGenres((String) genresObj);
    }

    Object releaseDateObj = doc.get("releaseDate");
    if (releaseDateObj instanceof java.util.Date) {
      dto.setReleaseDate((java.util.Date) releaseDateObj);
    }

    dto.setPosterPath(doc.getString("posterPath"));

    Object runtimeObj = doc.get("runtime");
    if (runtimeObj instanceof Number) {
      dto.setRuntime(((Number) runtimeObj).intValue());
    }

    dto.setOriginalLanguage(doc.getString("originalLanguage"));
    dto.setDirector(doc.getString("director"));

    if (doc.containsKey("score")) {
      Object scoreObj = doc.get("score");
      if (scoreObj instanceof Number) {
        dto.setScore(((Number) scoreObj).doubleValue());
      }
    }

    if (doc.containsKey("upVotes")) {
      Object upVotesObj = doc.get("upVotes");
      if (upVotesObj instanceof Number) {
        dto.setUpVotes(((Number) upVotesObj).intValue());
      }
    }

    if (doc.containsKey("downVotes")) {
      Object downVotesObj = doc.get("downVotes");
      if (downVotesObj instanceof Number) {
        dto.setDownVotes(((Number) downVotesObj).intValue());
      }
    }

    if (doc.containsKey("vote")) {
      List<?> voteList = (List<?>) doc.get("vote");
      if (voteList != null && !voteList.isEmpty()) {
        Document voteDoc = (Document) voteList.get(0);
        dto.setUserVote(voteDoc.getString("type"));
      }
    }
    return dto;
  }

  @Cacheable(value = "movies:details", key = "#p0", condition = "#id != null")
  public MovieDto getMovieById(String id) {
    Movie movie = movieRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("La película no existe"));
    return toDto(movie);
  }

  public MovieDto createMovie(CreateMovieDto movieDto) {
    Movie movie = toEntity(movieDto);
    if (movie.getGenres() == null || movie.getGenres().isEmpty()) {
      movie.setGenres("Uncategorized");
    }
    movie = movieRepository.save(movie);

    CreateDirectorDto directorDto = new CreateDirectorDto();
    directorDto.setDirector(movieDto.getDirector());
    directorDto.setMovieId(movie.getId());

    crudDirectorService.createDirector(directorDto);
    return toDto(movie);
  }

  public MovieDto updateMovie(String id, UpdateMovieDto movieDto) {
    return movieRepository
      .findById(id)
      .map(movie -> {
        if (movieDto.getExternalId() != 0) {
          movie.setExternalId(movieDto.getExternalId());
        }
        if (movieDto.getTitle() != null) {
          movie.setTitle(movieDto.getTitle());
        }
        if (movieDto.getOverview() != null) {
          movie.setOverview(movieDto.getOverview());
        }
        if (movieDto.getGenres() != null) {
          movie.setGenres(movieDto.getGenres());
        }
        if (movieDto.getReleaseDate() != null) {
          movie.setReleaseDate(movieDto.getReleaseDate());
        }
        if (movieDto.getPosterPath() != null) {
          movie.setPosterPath(movieDto.getPosterPath());
        }
        if (movieDto.getRuntime() != 0) {
          movie.setRuntime(movieDto.getRuntime());
        }
        if (movieDto.getOriginalLanguage() != null) {
          movie.setOriginalLanguage(movieDto.getOriginalLanguage());
        }
        Movie updatedMovie = movieRepository.save(movie);
        return toDto(updatedMovie);
      })
      .orElseThrow(() -> new NoSuchElementException("La película no existe"));
  }

  public void deleteMovie(String id) {
    if (!movieRepository.existsById(id)) {
      throw new NoSuchElementException("La película no existe");
    }
    movieRepository.deleteById(id);
  }

  private MovieDto toDto(Movie movie) {
    if (movie == null) {
      return null;
    }
    MovieDto dto = new MovieDto();
    dto.setId(movie.getId());
    dto.setExternalId(movie.getExternalId());
    dto.setTitle(movie.getTitle());
    dto.setOverview(movie.getOverview());
    dto.setGenres(movie.getGenres());
    dto.setReleaseDate(movie.getReleaseDate());
    dto.setPosterPath(movie.getPosterPath());
    dto.setRuntime(movie.getRuntime());
    dto.setOriginalLanguage(movie.getOriginalLanguage());
    dto.setUpVotes(movie.getUpVotes());
    dto.setDownVotes(movie.getDownVotes());

    var directorDto = crudDirectorService.getDirectorByMovieId(movie.getId());
    dto.setDirector(directorDto != null ? directorDto.getDirector() : null);

    return dto;
  }

  private Movie toEntity(CreateMovieDto dto) {
    if (dto == null) {
      return null;
    }
    Movie movie = new Movie();
    movie.setExternalId(dto.getExternalId());
    movie.setTitle(dto.getTitle());
    movie.setOverview(dto.getOverview());
    movie.setGenres(dto.getGenres());
    movie.setReleaseDate(dto.getReleaseDate());
    movie.setPosterPath(dto.getPosterPath());
    movie.setRuntime(dto.getRuntime());
    movie.setOriginalLanguage(dto.getOriginalLanguage());
    return movie;
  }

  public List<MovieDto> getTopMovies(int limit, String userId) {
    Aggregation aggregation = Aggregation.newAggregation(
      Aggregation.addFields()
        .addField("score")
        .withValue(
          new Document(
            "$subtract",
            Arrays.asList(
              new Document("$ifNull", Arrays.asList("$up_votes", 0)),
              new Document("$ifNull", Arrays.asList("$down_votes", 0))
            )
          )
        )
        .build(),
      Aggregation.sort(Direction.DESC, "score"),
      Aggregation.limit(limit),
      Aggregation.lookup("directors", "movie_id", "movie_id", "director"),
      Aggregation.unwind("director", true),
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'movie_votes', " +
          "  let: { movie_id_str: { $toString: '$_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $and: [ { $eq: ['$movie_id', '$$movie_id_str'] }, { $eq: ['$user_id', '" +
          userId +
          "'] } ] } } } " +
          "  ], " +
          "  as: 'vote' " +
          "} }"
      ),
      Aggregation.project()
        .and("_id")
        .as("id")
        .and("title")
        .as("title")
        .and("overview")
        .as("overview")
        .and("genres")
        .as("genres")
        .and("release_date")
        .as("releaseDate")
        .and("poster_path")
        .as("posterPath")
        .and("original_language")
        .as("originalLanguage")
        .and("runtime")
        .as("runtime")
        .and("director")
        .as("director")
        .and("score")
        .as("score")
        .and("up_votes")
        .as("upVotes")
        .and("down_votes")
        .as("downVotes")
        .and("vote")
        .as("vote")
    );

    AggregationResults<Document> results = mongoTemplate.aggregate(
      aggregation,
      "movies",
      Document.class
    );

    return results
      .getMappedResults()
      .stream()
      .map(doc -> {
        MovieDto dto = convertDocumentToMovieDto(doc);

        Document directorDoc = doc.get("director", Document.class);
        if (directorDoc != null) {
          dto.setDirector(directorDoc.getString("director"));
        }
        return dto;
      })
      .toList();
  }

  public MovieDto getRandomMovie() {
    Aggregation aggregation = Aggregation.newAggregation(
      Aggregation.sample(1),
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'directors', " +
          "  let: { movie_id_str: { $toString: '$_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: ['$movie_id', '$$movie_id_str'] } } } " +
          "  ], " +
          "  as: 'director_doc' " +
          "} }"
      ),
      Aggregation.unwind("director_doc", true),
      Aggregation.project()
        .and("_id")
        .as("id")
        .and("external_id")
        .as("externalId")
        .and("title")
        .as("title")
        .and("overview")
        .as("overview")
        .and("genres")
        .as("genres")
        .and("release_date")
        .as("releaseDate")
        .and("poster_path")
        .as("posterPath")
        .and("runtime")
        .as("runtime")
        .and("original_language")
        .as("originalLanguage")
        .and("up_votes")
        .as("upVotes")
        .and("down_votes")
        .as("downVotes")
        .and("director_doc.director")
        .as("director")
    );

    AggregationResults<Document> results = mongoTemplate.aggregate(
      aggregation,
      "movies",
      Document.class
    );

    Document doc = results.getUniqueMappedResult();
    if (doc == null) {
      throw new NoSuchElementException("No hay películas disponibles");
    }

    return convertDocumentToMovieDto(doc);
  }
}
