package com.example.intermediate.controller.response;

import com.example.intermediate.domain.Category;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
  private Long postId;
  private String title;
  private String content;
  private String author;
  private Category category;
  private long likesCount;
  private List<CommentResponseDto> commentResponseDtoList;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  
}
