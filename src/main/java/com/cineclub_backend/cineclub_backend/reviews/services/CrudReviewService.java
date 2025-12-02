package com.cineclub_backend.cineclub_backend.reviews.services;

import com.cineclub_backend.cineclub_backend.movies.dtos.MovieDto;
import com.cineclub_backend.cineclub_backend.movies.services.CrudMovieService;
import com.cineclub_backend.cineclub_backend.reviews.dots.CreateReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.FindReviewPagedDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.ReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.UpdateReviewDto;
import com.cineclub_backend.cineclub_backend.reviews.models.Review;
import com.cineclub_backend.cineclub_backend.reviews.repositories.ReviewRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
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
  private final ReviewRepository reviewRepository;
  private final CrudMovieService movieService;
  private final CrudCommentService commentService;
  private final CrudReviewLikeService reviewLikeService;

  public CrudReviewService(
    MongoTemplate mongoTemplate,
    ReviewRepository reviewRepository,
    CrudMovieService movieService,
    CrudCommentService commentService,
    CrudReviewLikeService reviewLikeService
  ) {
    this.mongoTemplate = mongoTemplate;
    this.reviewRepository = reviewRepository;
    this.movieService = movieService;
    this.commentService = commentService;
    this.reviewLikeService = reviewLikeService;
  }

  public Page<ReviewDto> getPagedReviews(
    FindReviewPagedDto params,
    String userId,
    String loggedUserId
  ) {
    Pageable pageable = params.toPageable();

    List<AggregationOperation> operations = new ArrayList<>();

    operations.add(
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'movies', " +
          "  let: { movie_id_str: { $toString: '$movie_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: [{ $toString: '$_id' }, '$$movie_id_str'] } } } " +
          "  ], " +
          "  as: 'movie' " +
          "} }"
      )
    );
    operations.add(Aggregation.unwind("movie", true));

    operations.add(
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'directors', " +
          "  let: { movie_id_str: { $toString: '$movie_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: ['$movie_id', '$$movie_id_str'] } } } " +
          "  ], " +
          "  as: 'director' " +
          "} }"
      )
    );
    operations.add(Aggregation.unwind("director", true));

    operations.add(
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'users', " +
          "  let: { user_id_str: { $toString: '$user_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: [{ $toString: '$_id' }, '$$user_id_str'] } } } " +
          "  ], " +
          "  as: 'user' " +
          "} }"
      )
    );
    operations.add(Aggregation.unwind("user", true));

    searchParams(params, operations, userId);

    FacetOperation facetOperation = Aggregation.facet()
      .and(Aggregation.count().as("total"))
      .as("metadata")
      .and(
        Aggregation.sort(pageable.getSort()),
        Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
        Aggregation.limit(pageable.getPageSize()),
        Aggregation.stage(
          "{$lookup: { " +
            "  from: 'review_likes', " +
            "  let: { review_id_str: { $toString: '$_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: ['$review_id', '$$review_id_str'] } } } " +
            "  ], " +
            "  as: 'review_likes' " +
            "} }"
        ),
        Aggregation.stage(
          "{$lookup: { " +
            "  from: 'comments', " +
            "  let: { review_id_str: { $toString: '$_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: ['$review_id', '$$review_id_str'] } } } " +
            "  ], " +
            "  as: 'comments' " +
            "} }"
        ),
        Aggregation.project()
          .and("_id")
          .as("id")
          .and("movie.title")
          .as("title")
          .and("content")
          .as("content")
          .and("rating")
          .as("rating")
          .and("user.fullName")
          .as("reviewerName")
          .and("director.director")
          .as("directorName")
          .and("movie.poster_path")
          .as("posterPath")
          .and("movie_id")
          .as("movieId")
          .and("user_id")
          .as("userId")
          .and("created_at")
          .as("createdAt")
          .and("updated_at")
          .as("updatedAt")
          .and("review_likes")
          .as("reviewLikes")
          .and("comments")
          .as("comments")
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
    List<ReviewDto> reviewDtos = data
      .stream()
      .map(doc -> documentToDto(doc, loggedUserId))
      .toList();

    return new PageImpl<>(reviewDtos, pageable, total);
  }

  private void searchParams(
    FindReviewPagedDto params,
    List<AggregationOperation> operations,
    String userId
  ) {
    if (params.getTitle() != null) {
      operations.add(
        Aggregation.match(Criteria.where("movie.title").regex(params.getTitle(), "i"))
      );
    }
    if (userId != null) {
      operations.add(Aggregation.match(Criteria.where("user_id").is(userId)));
    }
    if (params.getMovieId() != null) {
      operations.add(Aggregation.match(Criteria.where("movie_id").is(params.getMovieId())));
    }
    if (params.getGender() != null) {
      operations.add(
        Aggregation.match(Criteria.where("movie.genres").regex(params.getGender(), "i"))
      );
    }
    if (params.getRating() != null) {
      operations.add(Aggregation.match(Criteria.where("rating").is(params.getRating())));
    }
    if (params.getDirectorId() != null) {
      operations.add(Aggregation.match(Criteria.where("director._id").is(params.getDirectorId())));
    }
    if (params.getStartDate() != null) {
      operations.add(Aggregation.match(Criteria.where("created_at").gte(params.getStartDate())));
    }
    if (params.getEndDate() != null) {
      operations.add(Aggregation.match(Criteria.where("created_at").lte(params.getEndDate())));
    }
  }

  public Review findById(String id) {
    return reviewRepository.findById(id).orElse(null);
  }

  public ReviewDto getReviewById(String id, String userId) {
    List<AggregationOperation> operations = new ArrayList<>();
    operations.add(Aggregation.match(Criteria.where("_id").is(id)));
    operations.add(
      Aggregation.stage(
        "{ $lookup: { " +
          "  from: 'movies', " +
          "  let: { movie_id_str: { $toString: '$movie_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: [{ $toString: '$_id' }, '$$movie_id_str'] } } } " +
          "  ], " +
          "  as: 'movie' " +
          "} }"
      )
    );
    operations.add(
      Aggregation.stage(
        "{$lookup: { " +
          "  from: 'review_likes', " +
          "  let: { review_id_str: { $toString: '$_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: ['$review_id', '$$review_id_str'] } } } " +
          "  ], " +
          "  as: 'review_likes' " +
          "} }"
      )
    );
    operations.add(
      Aggregation.stage(
        "{$lookup: { " +
          "  from: 'comments', " +
          "  let: { review_id_str: { $toString: '$_id' } }, " +
          "  pipeline: [ " +
          "    { $match: { $expr: { $eq: ['$review_id', '$$review_id_str'] } } } " +
          "  ], " +
          "  as: 'comments' " +
          "} }"
      )
    );
    operations.add(Aggregation.lookup("directors", "movie_id", "movie_id", "director"));
    operations.add(Aggregation.lookup("users", "user_id", "_id", "user"));
    operations.add(Aggregation.unwind("movie", true));
    operations.add(Aggregation.unwind("director", true));
    operations.add(Aggregation.unwind("user", true));
    operations.add(
      Aggregation.project()
        .and("_id")
        .as("id")
        .and("movie.title")
        .as("title")
        .and("content")
        .as("content")
        .and("rating")
        .as("rating")
        .and("user.fullName")
        .as("reviewerName")
        .and("director.director")
        .as("directorName")
        .and("movie.poster_path")
        .as("posterPath")
        .and("movie_id")
        .as("movieId")
        .and("user_id")
        .as("userId")
        .and("created_at")
        .as("createdAt")
        .and("updated_at")
        .as("updatedAt")
        .and("review_likes")
        .as("reviewLikes")
        .and("comments")
        .as("comments")
    );

    Aggregation aggregation = Aggregation.newAggregation(operations);
    AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
      aggregation,
      "reviews",
      Document.class
    );
    Document result = aggregationResults.getUniqueMappedResult();
    if (result == null || result.isEmpty()) {
      throw new NoSuchElementException("Review no encontrada");
    }
    return documentToDto(result, userId);
  }

  public String createReview(CreateReviewDto dto, String userId) {
    MovieDto movie = movieService.getMovieById(dto.getMovieId());

    Review review = new Review();

    review.setContent(dto.getContent());
    review.setRating(dto.getRating());
    review.setUserId(userId);
    review.setMovieId(movie.getId());
    review.setCreatedAt(new Date());
    review.setUpdatedAt(new Date());
    reviewRepository.save(review);

    return review.getId();
  }

  public ReviewDto updateReview(String id, UpdateReviewDto dto, String userId) {
    Review review = reviewRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("Review no encontrada"));

    review.setContent(dto.getContent());
    review.setRating(dto.getRating());
    review.setUpdatedAt(new Date());

    reviewRepository.save(review);

    return getReviewById(id, userId);
  }

  public String deleteReview(String id, String userId) {
    Review review = reviewRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("Review no encontrada"));

    if (!review.getUserId().equals(userId)) {
      throw new SecurityException("No tienes permiso para eliminar esta reseña");
    }

    reviewRepository.deleteById(review.getId());
    commentService.deleteAllCommentsByReviewId(id);
    reviewLikeService.removeAllLikeReview(id);

    return review.getId();
  }

  private ReviewDto documentToDto(Document doc, String userId) {
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

    List<Document> likesDocs = doc.getList("reviewLikes", Document.class);
    if (likesDocs != null) {
      dto.setLikes(likesDocs.size());
      boolean isLiked = likesDocs
        .stream()
        .anyMatch(like -> userId != null && userId.equals(like.getString("user_id")));
      dto.setLiked(isLiked);
    } else {
      dto.setLikes(0);
      dto.setLiked(false);
    }

    List<Document> commentsDocs = doc.getList("comments", Document.class);
    if (commentsDocs != null) {
      dto.setComments(commentsDocs.size());
    } else {
      dto.setComments(0);
    }

    return dto;
  }
}
