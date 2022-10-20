package com.example.intermediate.controller;

import com.example.intermediate.controller.request.CommentRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping(value = "/write")
    public ResponseDto<?> writeComment(@RequestBody CommentRequestDto commentRequestDto, HttpServletRequest httpRequest) {
        return commentService.writeComment(commentRequestDto, httpRequest);
    }

    // 댓글 수정
    @PutMapping(value = "/edit/{commentId}")
    public ResponseDto<?> updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto commentRequestDto, HttpServletRequest httpRequest) {
        return commentService.updatecomment(commentId, commentRequestDto, httpRequest);
    }

    // 댓글 삭제
    @DeleteMapping(value = "/remove/{commentId}")
    public ResponseDto<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        return commentService.deleteComment(commentId, request);
    }

}
