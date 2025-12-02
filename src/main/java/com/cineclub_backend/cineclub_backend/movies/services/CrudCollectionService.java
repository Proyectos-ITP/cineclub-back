package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.CollectionResponseDto;
import com.cineclub_backend.cineclub_backend.movies.models.Collection;
import com.cineclub_backend.cineclub_backend.movies.repositories.CollectionRepository;
import com.cineclub_backend.cineclub_backend.shared.dtos.PagedResponseDto;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class CrudCollectionService {

  private final CollectionRepository collectionRepository;
  private final MongoTemplate mongoTemplate;

  public CrudCollectionService(
    CollectionRepository collectionRepository,
    MongoTemplate mongoTemplate
  ) {
    this.collectionRepository = collectionRepository;
    this.mongoTemplate = mongoTemplate;
  }

  public PagedResponseDto<CollectionResponseDto> getPagedCollections(
    String title,
    String userId,
    Pageable pageable
  ) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      if (userId != null && !userId.trim().isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("user_id").is(userId)));
      }

      operations.add(
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'movies', " +
            "  let: { movie_ids: '$movies' }, " +
            "  pipeline: [ " +
            "    { $addFields: { " +
            "      id_as_string: { $toString: '$_id' } " +
            "    } }, " +
            "    { $match: { " +
            "      $expr: { $in: ['$id_as_string', '$$movie_ids'] } " +
            "    } } " +
            "  ], " +
            "  as: 'movieDetails' " +
            "} }"
        )
      );

      if (title != null && !title.trim().isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("movieDetails.title").regex(title, "i")));
      }

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize()),
          Aggregation.project()
            .and("_id")
            .as("_id")
            .and("user_id")
            .as("user_id")
            .and("movieDetails")
            .as("movies")
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);

      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "collections",
        Document.class
      );

      Document result = results.getUniqueMappedResult();

      if (result == null) {
        return new PagedResponseDto<>(new PageImpl<>(new ArrayList<>(), pageable, 0));
      }

      long total = 0;
      Object metadataObj = result.get("metadata");
      if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
        Object firstItem = metadataList.get(0);
        if (firstItem instanceof Document metadataDoc) {
          total = metadataDoc.getInteger("total", 0);
        }
      }

      List<CollectionResponseDto> collectionDtos = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            collectionDtos.add(convertDocumentToCollectionResponseDto(doc));
          }
        }
      }

      Page<CollectionResponseDto> page = new PageImpl<>(collectionDtos, pageable, total);
      return new PagedResponseDto<>(page);
    } catch (Exception e) {
      throw e;
    }
  }

  private CollectionResponseDto convertDocumentToCollectionResponseDto(Document doc) {
    CollectionResponseDto dto = new CollectionResponseDto();

    Object idObj = doc.get("_id");
    if (idObj != null) {
      dto.setId(idObj.toString());
    }

    Object userIdObj = doc.get("user_id");
    if (userIdObj != null) {
      dto.setUserId(userIdObj.toString());
    }

    Object moviesObj = doc.get("movies");
    if (moviesObj instanceof List<?> moviesList) {
      List<CollectionResponseDto.MovieInfo> movieInfoList = new ArrayList<>();
      for (Object movieItem : moviesList) {
        if (movieItem instanceof Document movieDoc) {
          CollectionResponseDto.MovieInfo movieInfo = new CollectionResponseDto.MovieInfo();

          Object movieIdObj = movieDoc.get("_id");
          if (movieIdObj != null) {
            movieInfo.setId(movieIdObj.toString());
          }

          movieInfo.setTitle(movieDoc.getString("title"));
          movieInfo.setPosterPath(movieDoc.getString("poster_path"));
          movieInfo.setOverview(movieDoc.getString("overview"));
          movieInfo.setReleaseDate(movieDoc.getDate("release_date"));
          movieInfoList.add(movieInfo);
        }
      }
      dto.setMovies(movieInfoList);
    }

    return dto;
  }

  public CollectionDto createCollectionItem(String movie_id, String userId) {
    Collection collectionByUserExist = collectionRepository.findByUserId(userId).orElse(null);
    if (collectionByUserExist != null) {
      List<String> movies = collectionByUserExist.getMovies();
      if (movies.contains(movie_id)) {
        return toDto(collectionByUserExist);
      }
      movies.add(movie_id);
      collectionByUserExist.setMovies(movies);
      Collection collection = collectionRepository.save(collectionByUserExist);
      return toDto(collection);
    }
    return toDto(collectionRepository.save(toEntity(movie_id, userId)));
  }

  public String deleteCollectionItem(String movie_id, String userId) {
    Collection collectionByUserExist = collectionRepository.findByUserId(userId).orElse(null);
    if (collectionByUserExist != null) {
      List<String> movies = collectionByUserExist.getMovies();
      if (movies.contains(movie_id)) {
        movies.remove(movie_id);
        collectionByUserExist.setMovies(movies);
        Collection collection = collectionRepository.save(collectionByUserExist);
        return collection.getId();
      }
    }
    return "";
  }

  public Collection toEntity(String movie_id, String userId) {
    if (movie_id == null || userId == null) {
      return null;
    }
    Collection collection = new Collection();
    collection.setMovies(List.of(movie_id));
    collection.setUserId(userId);
    return collection;
  }

  private CollectionDto toDto(Collection collection) {
    if (collection == null) {
      return null;
    }
    CollectionDto dto = new CollectionDto();
    dto.setId(collection.getId());
    dto.setMovies(collection.getMovies());
    dto.setUserId(collection.getUserId());
    return dto;
  }
}
