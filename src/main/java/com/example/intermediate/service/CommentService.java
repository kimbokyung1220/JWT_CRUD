package com.example.intermediate.service;

import com.example.intermediate.controller.request.CommentRequestDto;
import com.example.intermediate.controller.response.CommentResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Comment;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TokenProvider tokenProvider;
    private final PostService postService;

    // 게시글 존재여부
    @Transactional(readOnly = true)
    public Comment isPresentComment(Long id) {
        Optional<Comment> optionalComment = commentRepository.findById(id);
        return optionalComment.orElse(null);
    }

    // token여부
    @Transactional
    public  Member validateMember(HttpServletRequest httpRequest) {
        // 1. 로그인 후 게시글을 작성할 수 있다. (Header에 토큰값 확인하기)
        // 1-1. Refresh-Token 확인
        if(httpRequest.getHeader("Refresh-Token") == null) {
            throw new IllegalArgumentException(" *로그인 필요* Refresh-Token값이 존재하지 않습니다.");
        }
        // 1-2. Authorization 확인
        if(httpRequest.getHeader("Authorization") == null) {
            throw new IllegalArgumentException(" *로그인 필요* Authorization값이 존재하지 않습니다.");
        }

        if (!tokenProvider.validateToken(httpRequest.getHeader("Refresh-Token"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }

    // 댓글 작성
    @Transactional
    public ResponseDto<?> writeComment(CommentRequestDto commentRequestDto, HttpServletRequest httpRequest) {
        Member memberInfo = validateMember(httpRequest);
        if (memberInfo == null) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Post post = postService.isPresentPost(commentRequestDto.getPostId());
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        CommentResponseDto commentList = null;
        if (commentRequestDto.getParentCommentId() == null) {
            Comment comment = Comment.builder()
                    .member(memberInfo)
                    .post(post)
                    .content(commentRequestDto.getContent())
                    .author(memberInfo.getName())
                    .build();
            commentRepository.save(comment);
            commentList = CommentResponseDto.builder()
                    .commentId(comment.getCommentId())
                    .content(comment.getContent())
                    .author(comment.getAuthor())
                    .parentCommentId(comment.getParentCommentId())
                    .createdAt(comment.getCreatedAt())
                    .modifiedAt(comment.getModifiedAt())
                    .build();
            return ResponseDto.success(commentList);
        }
        Comment comment = Comment.builder()
                .member(memberInfo)
                .post(post)
                .content(commentRequestDto.getContent())
                .author(memberInfo.getName())
                .parentCommentId(commentRequestDto.getParentCommentId())
                .build();
        commentRepository.save(comment);

        commentList = CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .parentCommentId(comment.getParentCommentId())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
        return ResponseDto.success(commentList);
    }

    // 댓글 수정
    public ResponseDto<?> updatecomment(Long commentId, CommentRequestDto commentRequestDto, HttpServletRequest httpRequest) {
        Member memberInfo = validateMember(httpRequest);
        if (memberInfo == null) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Post post = postService.isPresentPost(commentRequestDto.getPostId());
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        Comment comment = isPresentComment(commentId);
        if (comment == null) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
        }
        if (comment.validateMember(memberInfo)) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }

        comment.update(commentRequestDto);
        CommentResponseDto commentList = CommentResponseDto.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .parentCommentId(comment.getParentCommentId())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
        return ResponseDto.success(commentList);

    }

    // 댓글 삭제
    @Transactional
    public ResponseDto<?> deleteComment(Long commentId, HttpServletRequest request) {
        Member memberInfo = validateMember(request);
        if (null == memberInfo) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Comment comment = isPresentComment(commentId);
        if (null == comment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
        }

        if (comment.validateMember(memberInfo)) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }

        commentRepository.deleteByCommentIdOrParentCommentId(commentId, commentId);
        return ResponseDto.success("success");
    }


}
