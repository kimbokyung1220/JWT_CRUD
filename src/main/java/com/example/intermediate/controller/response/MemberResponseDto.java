package com.example.intermediate.controller.response;

import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
  private Long id;
  private String email;
  private String name;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

}
