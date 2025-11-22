package com.cineclub_backend.cineclub_backend.reviews.dots;

import com.cineclub_backend.cineclub_backend.shared.dtos.PaginationDto;
import java.util.Date;
import lombok.Data;

@Data
public class FindReviewPagedDto extends PaginationDto {

  private String title;
  private String userId;
  private String movieId;
  private String gender;
  private String directorId;
  private Integer rating;
  private Date startDate;
  private Date endDate;
}
