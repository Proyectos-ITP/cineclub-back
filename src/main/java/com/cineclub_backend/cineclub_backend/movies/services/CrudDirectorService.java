package com.cineclub_backend.cineclub_backend.movies.services;

import com.cineclub_backend.cineclub_backend.movies.dtos.CreateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.DirectorDto;
import com.cineclub_backend.cineclub_backend.movies.dtos.UpdateDirectorDto;
import com.cineclub_backend.cineclub_backend.movies.models.Director;
import com.cineclub_backend.cineclub_backend.movies.repositories.DirectorsRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
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
public class CrudDirectorService {

  private final DirectorsRepository directorsRepository;
  private final MongoTemplate mongoTemplate;
  private final ApplicationContext applicationContext;

  public CrudDirectorService(
    DirectorsRepository directorsRepository,
    MongoTemplate mongoTemplate,
    ApplicationContext applicationContext
  ) {
    this.directorsRepository = directorsRepository;
    this.mongoTemplate = mongoTemplate;
    this.applicationContext = applicationContext;
  }

  public Page<DirectorDto> getPagedDirectorsWithMovies(String director, Pageable pageable) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      if (director != null && !director.isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("director").regex(director, "i")));
      }

      operations.add(
        Aggregation.group("director")
          .first("director")
          .as("directorName")
          .addToSet("movie_id")
          .as("movieIds")
      );
      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize()),
          Aggregation.stage(
            "{ $lookup: { " +
              "  from: 'movies', " +
              "  let: { movie_ids: '$movieIds' }, " +
              "  pipeline: [ " +
              "    { $addFields: { id_as_string: { $toString: '$_id' } } }, " +
              "    { $match: { $expr: { $in: ['$id_as_string', '$$movie_ids'] } } }, " +
              "    { $project: { id: { $toString: '$_id' }, title: 1 } } " +
              "  ], " +
              "  as: 'movies' " +
              "} }"
          )
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);

      AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
        aggregation,
        "directors",
        Document.class
      );

      Document result = aggregationResults.getUniqueMappedResult();

      if (result == null) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
      }

      long total = 0;
      Object metadataObj = result.get("metadata");
      if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
        Object firstItem = metadataList.get(0);
        if (firstItem instanceof Document metadataDoc) {
          total = metadataDoc.getInteger("total", 0);
        }
      }

      List<DirectorDto> directorDtos = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            directorDtos.add(convertDocumentToDirectorDto(doc, true));
          }
        }
      }

      return new PageImpl<>(directorDtos, pageable, total);
    } catch (Exception e) {
      throw e;
    }
  }

  public Page<DirectorDto> getPagedDirectors(String director, Pageable pageable) {
    try {
      List<AggregationOperation> operations = new ArrayList<>();

      Criteria directorCriteria = Criteria.where("director").ne(null).ne("").regex("\\S");

      if (director != null && !director.isEmpty()) {
        operations.add(Aggregation.match(Criteria.where("director").regex(director, "i")));
      }

      operations.add(Aggregation.match(directorCriteria));
      operations.add(Aggregation.group("director"));

      operations.add(Aggregation.sort(org.springframework.data.domain.Sort.by("_id")));

      operations.add(Aggregation.project().andExpression("_id").as("director"));

      FacetOperation facetOperation = Aggregation.facet()
        .and(Aggregation.count().as("total"))
        .as("metadata")
        .and(
          Aggregation.sort(pageable.getSort()),
          Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
          Aggregation.limit(pageable.getPageSize())
        )
        .as("data");

      operations.add(facetOperation);

      Aggregation aggregation = Aggregation.newAggregation(operations);

      AggregationResults<Document> results = mongoTemplate.aggregate(
        aggregation,
        "directors",
        Document.class
      );

      Document result = results.getUniqueMappedResult();

      if (result == null) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
      }

      long total = 0;
      Object metadataObj = result.get("metadata");
      if (metadataObj instanceof List<?> metadataList && !metadataList.isEmpty()) {
        Object firstItem = metadataList.get(0);
        if (firstItem instanceof Document metadataDoc) {
          total = metadataDoc.getInteger("total", 0);
        }
      }

      List<DirectorDto> directorDtos = new ArrayList<>();
      Object dataObj = result.get("data");
      if (dataObj instanceof List<?> dataList) {
        for (Object item : dataList) {
          if (item instanceof Document doc) {
            directorDtos.add(convertDocumentToDirectorDto(doc, false));
          }
        }
      }

      return new PageImpl<>(directorDtos, pageable, total);
    } catch (Exception e) {
      throw e;
    }
  }

  private DirectorDto convertDocumentToDirectorDto(Document doc, boolean withMovies) {
    DirectorDto dto = new DirectorDto();

    Object directorNameObj = doc.get("_id");
    if (directorNameObj != null) {
      dto.setDirector(directorNameObj.toString());
    }
    String directorName = doc.getString("directorName");
    if (directorName != null) {
      dto.setDirector(directorName);
    }

    if (withMovies) {
      List<DirectorDto.MovieInfo> movieInfoList = new ArrayList<>();
      Object moviesObj = doc.get("movies");
      if (moviesObj instanceof List<?> moviesList) {
        for (Object movieItem : moviesList) {
          if (movieItem instanceof Document movieDoc) {
            DirectorDto.MovieInfo movieInfo = new DirectorDto.MovieInfo();

            Object movieIdObj = movieDoc.get("id");
            if (movieIdObj != null) {
              movieInfo.setId(movieIdObj.toString());
            }

            movieInfo.setTitle(movieDoc.getString("title"));
            movieInfoList.add(movieInfo);
          }
        }
      }
      dto.setMovies(movieInfoList);
    }

    return dto;
  }

  @Cacheable(value = "directors:details", key = "#movieId")
  public DirectorDto getDirectorByMovieId(String movieId) {
    return directorsRepository.findByMovieId(movieId).map(this::toDto).orElse(null);
  }

  public DirectorDto getDirectorById(String id) {
    Director director = directorsRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("El director no existe."));

    CrudDirectorService self = applicationContext.getBean(CrudDirectorService.class);

    return self.getDirectorDetailsByName(director.getDirector());
  }

  @Cacheable(value = "directors:details", key = "#directorName")
  public DirectorDto getDirectorDetailsByName(String directorName) {
    List<AggregationOperation> operations = new ArrayList<>();
    operations.add(Aggregation.match(Criteria.where("director").is(directorName)));
    operations.add(
      Aggregation.group("director")
        .first("director")
        .as("directorName")
        .addToSet("movie_id")
        .as("movieIds")
    );

    operations.add(
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'movies', " +
          "  let: { movie_ids: '$movieIds' }, " +
          "  pipeline: [ " +
          "    { $addFields: { id_as_string: { $toString: '$_id' } } }, " +
          "    { $match: { $expr: { $in: ['$id_as_string', '$$movie_ids'] } } }, " +
          "    { $project: { id: { $toString: '$_id' }, title: 1, _id: 0 } } " +
          "  ], " +
          "  as: 'movies' " +
          "} }"
      )
    );

    operations.add(
      Aggregation.project().and("directorName").as("directorName").and("movies").as("movies")
    );

    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<Document> results = mongoTemplate.aggregate(
      aggregation,
      "directors",
      Document.class
    );
    Document result = results.getUniqueMappedResult();

    if (result == null) {
      throw new NoSuchElementException("El director no existe.");
    }

    return convertDocumentToDirectorDto(result, true);
  }

  public DirectorDto createDirector(CreateDirectorDto directorDto) {
    Director director = toEntity(directorDto);
    director = directorsRepository.save(director);
    return toDto(director);
  }

  public DirectorDto updateDirector(String id, UpdateDirectorDto directorDto) {
    return directorsRepository
      .findById(id)
      .map(director -> {
        director.setDirector(directorDto.getDirector());
        director.setMovieId(directorDto.getMovieId());
        Director updatedDirector = directorsRepository.save(director);
        return toDto(updatedDirector);
      })
      .orElseThrow(() -> new NoSuchElementException("El director no existe."));
  }

  public void deleteDirector(String id) {
    if (!directorsRepository.existsById(id)) {
      throw new NoSuchElementException("El director no existe.");
    }
    directorsRepository.deleteById(id);
  }

  private DirectorDto toDto(Director director) {
    if (director == null) {
      return null;
    }
    DirectorDto dto = new DirectorDto();
    dto.setId(director.getId());
    dto.setDirector(director.getDirector());
    return dto;
  }

  public Director toEntity(CreateDirectorDto dto) {
    if (dto == null) {
      return null;
    }
    Director director = new Director();
    director.setDirector(dto.getDirector());
    director.setMovieId(dto.getMovieId());
    return director;
  }
}
