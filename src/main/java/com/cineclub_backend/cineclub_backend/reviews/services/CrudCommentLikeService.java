package com.cineclub_backend.cineclub_backend.reviews.services;

import com.cineclub_backend.cineclub_backend.reviews.models.CommentLike;
import com.cineclub_backend.cineclub_backend.reviews.repositories.CommentLikeRepository;
import org.springframework.stereotype.Service;

@Service
public class CrudCommentLikeService {

  private final CommentLikeRepository commentLikeRepository;

  public CrudCommentLikeService(CommentLikeRepository commentLikeRepository) {
    this.commentLikeRepository = commentLikeRepository;
  }

  public String createCommentLike(String commentId, String userId) {
    CommentLike commentLikeExists = commentLikeRepository.findByCommentIdAndUserId(
      commentId,
      userId
    );
    if (commentLikeExists != null) {
      throw new RuntimeException("Ya has dado like a este comentario");
    }
    CommentLike commentLike = new CommentLike();
    commentLike.setCommentId(commentId);
    commentLike.setUserId(userId);
    commentLikeRepository.save(commentLike);
    return commentLike.getId();
  }

  public String deleteCommentLike(String commentId, String userId) {
    CommentLike commentLikeExists = commentLikeRepository.findByCommentIdAndUserId(
      commentId,
      userId
    );
    if (commentLikeExists == null) {
      throw new RuntimeException("No has dado like a este comentario");
    }
    commentLikeRepository.delete(commentLikeExists);
    return commentLikeExists.getId();
  }

  public void deleteAllCommentLikesByCommentId(String commentId) {
    commentLikeRepository.deleteAllByCommentId(commentId);
  }

  public void deleteAllCommentLikesByCommentIds(java.util.List<String> commentIds) {
    commentLikeRepository.deleteAllByCommentIdIn(commentIds);
  }
}
