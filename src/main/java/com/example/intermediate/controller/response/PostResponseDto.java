package com.example.intermediate.controller.response;

import com.example.intermediate.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
  private Long id;
  private String title;
  private String content;
  private Category category;
  private String author;
  private List<CommentResponseDto> commentResponseDtoList;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
}
