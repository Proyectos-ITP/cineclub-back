package com.cineclub_backend.cineclub_backend.reviews.services;

import com.cineclub_backend.cineclub_backend.reviews.dots.FindReviewPagedDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.ReviewDto;
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
public class CrudReviewService {

  private final MongoTemplate mongoTemplate;

  public CrudReviewService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Page<ReviewDto> getPagedReviews(FindReviewPagedDto params) {
    Pageable pageable = params.toPageable();

    List<AggregationOperation> operations = new ArrayList<>();
    searchParams(params, operations);

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
            "  let: { movie_id_str: { $toString: '$movie_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: ['$movie_id', '$$movie_id_str'] } } } " +
            "  ], " +
            "  as: 'movie' " +
            "} }"
        ),
        Aggregation.unwind("movie", true),
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
        Aggregation.project()
          .and("_id")
          .as("id")
          .and("title")
          .as("title")
          .and("content")
          .as("content")
          .and("rating")
          .as("rating")
          .and("reviewerName")
          .as("reviewerName")
          .and("directorName")
          .as("directorName")
          .and("posterPath")
          .as("posterPath")
          .and("movieId")
          .as("movieId")
          .and("userId")
          .as("userId")
          .and("createdAt")
          .as("createdAt")
          .and("updatedAt")
          .as("updatedAt")
      )
      .as("data");

    operations.add(facetOperation);
    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
      aggregation,
      "reviews",
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
    List<ReviewDto> reviewDtos = data.stream().map(this::toDto).toList();

    return new PageImpl<>(reviewDtos, pageable, total);
  }

  private void searchParams(FindReviewPagedDto params, List<AggregationOperation> operations) {
    if (params.getTitle() != null) {
      operations.add(Aggregation.match(Criteria.where("title").regex(params.getTitle())));
    }
    if (params.getUserId() != null) {
      operations.add(Aggregation.match(Criteria.where("userId").is(params.getUserId())));
    }
    if (params.getMovieId() != null) {
      operations.add(Aggregation.match(Criteria.where("movieId").is(params.getMovieId())));
    }
    if (params.getGender() != null) {
      operations.add(Aggregation.match(Criteria.where("gender").in(params.getGender())));
    }
    if (params.getDirectorId() != null) {
      operations.add(Aggregation.match(Criteria.where("directorId").is(params.getDirectorId())));
    }
    if (params.getStartDate() != null) {
      operations.add(Aggregation.match(Criteria.where("createdAt").gte(params.getStartDate())));
    }
    if (params.getEndDate() != null) {
      operations.add(Aggregation.match(Criteria.where("createdAt").lte(params.getEndDate())));
    }
  }

  private ReviewDto toDto(Document doc) {
    ReviewDto dto = new ReviewDto();

    Object idObj = doc.get("id");
    if (idObj != null) {
      dto.setId(idObj.toString());
    }

    dto.setTitle(doc.getString("title"));
    dto.setContent(doc.getString("content"));
    dto.setRating(doc.getInteger("rating"));
    dto.setReviewerName(doc.getString("reviewerName"));
    dto.setDirectorName(doc.getString("directorName"));
    dto.setPosterPath(doc.getString("posterPath"));
    dto.setMovieId(doc.getString("movieId"));
    dto.setUserId(doc.getString("userId"));
    dto.setCreatedAt(doc.getDate("createdAt"));
    dto.setUpdatedAt(doc.getDate("updatedAt"));
    return dto;
  }
}
