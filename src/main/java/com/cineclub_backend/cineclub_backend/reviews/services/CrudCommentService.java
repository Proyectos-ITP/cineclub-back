package com.cineclub_backend.cineclub_backend.reviews.services;

import com.cineclub_backend.cineclub_backend.reviews.dots.CommentDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.CreateCommentDto;
import com.cineclub_backend.cineclub_backend.reviews.dots.UpdateCommentDto;
import com.cineclub_backend.cineclub_backend.reviews.models.Comment;
import com.cineclub_backend.cineclub_backend.reviews.models.Review;
import com.cineclub_backend.cineclub_backend.reviews.repositories.CommentRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import org.bson.Document;
import org.springframework.context.annotation.Lazy;
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
public class CrudCommentService {

  private final MongoTemplate mongoTemplate;
  private final CommentRepository commentRepository;
  private final CrudReviewService crudReviewService;
  private final CrudCommentLikeService crudCommentLikeService;

  public CrudCommentService(
    MongoTemplate mongoTemplate,
    CommentRepository commentRepository,
    @Lazy CrudReviewService crudReviewService,
    CrudCommentLikeService crudCommentLikeService
  ) {
    this.mongoTemplate = mongoTemplate;
    this.commentRepository = commentRepository;
    this.crudReviewService = crudReviewService;
    this.crudCommentLikeService = crudCommentLikeService;
  }

  public Page<CommentDto> findPagedByReviewId(String reviewId, String userId, Pageable pageable) {
    List<AggregationOperation> operations = new ArrayList<>();

    operations.add(
      Aggregation.match(Criteria.where("review_id").is(reviewId).and("parent_id").is(null))
    );

    FacetOperation facet = Aggregation.facet()
      .and(Aggregation.count().as("total"))
      .as("metadata")
      .and(
        Aggregation.sort(pageable.getSort()),
        Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
        Aggregation.limit(pageable.getPageSize()),
        Aggregation.lookup("users", "user_id", "_id", "userDetails"),
        Aggregation.unwind("userDetails", true),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'comments', " +
            "  let: { root_id: { $toString: '$_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: ['$parent_id', '$$root_id'] } } }, " +
            "    { $lookup: { " +
            "        from: 'users', " +
            "        localField: 'user_id', " +
            "        foreignField: '_id', " +
            "        as: 'replyUserDetails' " +
            "    } }, " +
            "    { $unwind: { path: '$replyUserDetails', preserveNullAndEmptyArrays: true } }, " +
            "    { $lookup: { " +
            "        from: 'comment_likes', " +
            "        let: { reply_id: { $toString: '$_id' } }, " +
            "        pipeline: [ " +
            "          { $match: { $expr: { $eq: ['$comment_id', '$$reply_id'] } } } " +
            "        ], " +
            "        as: 'replyLikes' " +
            "    } }, " +
            "    { $sort: { created_at: 1 } } " +
            "  ], " +
            "  as: 'replies' " +
            "} }"
        ),
        Aggregation.stage(
          "{ $lookup: { " +
            "  from: 'comment_likes', " +
            "  let: { root_id: { $toString: '$_id' } }, " +
            "  pipeline: [ " +
            "    { $match: { $expr: { $eq: ['$comment_id', '$$root_id'] } } }, " +
            "    { $sort: { created_at: 1 } } " +
            "  ], " +
            "  as: 'likes' " +
            "} }"
        )
      )
      .as("data");

    operations.add(facet);

    Aggregation aggregation = Aggregation.newAggregation(operations);

    AggregationResults<Document> results = mongoTemplate.aggregate(
      aggregation,
      "comments",
      Document.class
    );

    Document result = results.getUniqueMappedResult();
    if (result == null) {
      return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    List<Document> metadata = result.getList("metadata", Document.class);
    long total = metadata.isEmpty() ? 0 : metadata.get(0).getInteger("total", 0);

    List<Document> data = result.getList("data", Document.class);
    List<CommentDto> dtos = data
      .stream()
      .map(doc -> documentToDto(doc, userId))
      .toList();

    return new PageImpl<>(dtos, pageable, total);
  }

  public String createComment(CreateCommentDto commentDto, String userId) {
    Review reviewExists = crudReviewService.findById(commentDto.getReviewId());

    if (reviewExists == null) {
      throw new NoSuchElementException("Review no encontrado");
    }

    Comment comment = new Comment();
    comment.setReviewId(commentDto.getReviewId());
    comment.setUserId(userId);
    comment.setContent(commentDto.getContent());
    comment.setCreatedAt(new Date());
    comment.setUpdatedAt(new Date());
    comment = commentRepository.save(comment);
    return comment.getId();
  }

  public String createReply(CreateCommentDto commentDto, String parentId, String userId) {
    Review reviewExists = crudReviewService.findById(commentDto.getReviewId());

    if (reviewExists == null) {
      throw new NoSuchElementException("Review no encontrado");
    }

    Comment comment = new Comment();
    comment.setReviewId(commentDto.getReviewId());
    comment.setUserId(userId);
    comment.setContent(commentDto.getContent());
    comment.setCreatedAt(new Date());
    comment.setUpdatedAt(new Date());
    comment.setParentId(parentId);
    comment = commentRepository.save(comment);
    return comment.getId();
  }

  public String deleteComment(String id, String userId) {
    Comment comment = commentRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("Comentario no encontrado"));

    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("No tienes permiso para eliminar este comentario");
    }

    commentRepository.deleteById(id);
    crudCommentLikeService.deleteAllCommentLikesByCommentId(id);
    deleteReplies(id);
    return id;
  }

  public String deleteReply(String id, String userId) {
    Comment comment = commentRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("Comentario no encontrado"));

    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("No tienes permiso para eliminar este comentario");
    }

    commentRepository.deleteById(id);
    crudCommentLikeService.deleteAllCommentLikesByCommentId(id);
    return id;
  }

  public void deleteAllCommentsByReviewId(String reviewId) {
    List<String> commentIds = commentRepository
      .findAllByReviewId(reviewId)
      .stream()
      .map(Comment::getId)
      .toList();
    commentRepository.deleteAllByReviewId(reviewId);
    crudCommentLikeService.deleteAllCommentLikesByCommentIds(commentIds);
    deleteReplies(commentIds);
  }

  public void deleteReplies(List<String> commentIds) {
    commentRepository.deleteAllByParentIdIn(commentIds);
    crudCommentLikeService.deleteAllCommentLikesByCommentIds(commentIds);
  }

  public String updateComment(String id, UpdateCommentDto commentDto, String userId) {
    Comment comment = commentRepository
      .findById(id)
      .orElseThrow(() -> new NoSuchElementException("Comentario no encontrado"));

    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("No tienes permiso para actualizar este comentario");
    }

    comment.setContent(commentDto.getContent());
    comment.setUpdatedAt(new Date());
    commentRepository.save(comment);
    return comment.getId();
  }

  private void deleteReplies(String id) {
    List<Comment> replies = commentRepository.findByParentId(id);
    if (!replies.isEmpty()) {
      List<String> replyIds = replies.stream().map(Comment::getId).toList();
      crudCommentLikeService.deleteAllCommentLikesByCommentIds(replyIds);
      commentRepository.deleteByParentId(id);
    }
  }

  private CommentDto documentToDto(Document doc, String userId) {
    CommentDto dto = new CommentDto();
    dto.setId(doc.getObjectId("_id").toString());
    dto.setReviewId(doc.getString("review_id"));
    dto.setUserId(doc.getString("user_id"));
    dto.setContent(doc.getString("content"));
    dto.setCreatedAt(doc.getDate("created_at"));
    dto.setUpdatedAt(doc.getDate("updated_at"));

    Document userDetails = doc.get("userDetails", Document.class);
    if (userDetails != null) {
      dto.setUserName(userDetails.getString("fullName"));
    }

    List<Document> likesDocs = doc.getList("likes", Document.class);
    if (likesDocs != null) {
      dto.setLikes(likesDocs.size());
      boolean isLiked = likesDocs
        .stream()
        .anyMatch(like -> userId != null && userId.equals(like.getString("user_id")));
      dto.setLiked(isLiked);
    }

    List<Document> repliesDocs = doc.getList("replies", Document.class);
    if (repliesDocs != null) {
      List<CommentDto> replies = repliesDocs
        .stream()
        .map(replyDoc -> {
          CommentDto replyDto = new CommentDto();
          replyDto.setId(replyDoc.getObjectId("_id").toString());
          replyDto.setReviewId(replyDoc.getString("review_id"));
          replyDto.setUserId(replyDoc.getString("user_id"));
          replyDto.setContent(replyDoc.getString("content"));
          replyDto.setParentId(replyDoc.getString("parent_id"));
          replyDto.setCreatedAt(replyDoc.getDate("created_at"));

          Document replyUser = replyDoc.get("replyUserDetails", Document.class);
          if (replyUser != null) {
            replyDto.setUserName(replyUser.getString("fullName"));
          }

          List<Document> replyLikesDocs = replyDoc.getList("replyLikes", Document.class);
          if (replyLikesDocs != null) {
            replyDto.setLikes(replyLikesDocs.size());
            boolean isReplyLiked = replyLikesDocs
              .stream()
              .anyMatch(like -> userId != null && userId.equals(like.getString("user_id")));
            replyDto.setLiked(isReplyLiked);
          }
          return replyDto;
        })
        .toList();
      dto.setReplies(replies);
    }

    return dto;
  }
}
