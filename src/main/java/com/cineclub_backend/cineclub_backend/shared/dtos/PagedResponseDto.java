package com.cineclub_backend.cineclub_backend.shared.dtos;

import java.util.List;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class PagedResponseDto<T> {

  private List<T> data;
  private int page;
  private int size;
  private long total;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;

  public PagedResponseDto(Page<T> page) {
    this.data = page.getContent();
    this.page = page.getNumber() + 1;
    this.size = page.getNumberOfElements();
    this.total = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.hasNext = page.hasNext();
    this.hasPrevious = page.hasPrevious();
  }
}
