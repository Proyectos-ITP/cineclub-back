package com.cineclub_backend.cineclub_backend.social.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
public class FriendDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;

  private List<String> friends;
}
