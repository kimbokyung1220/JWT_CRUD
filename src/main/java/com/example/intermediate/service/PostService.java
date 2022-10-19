package com.example.intermediate.service;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;

    // 토큰값 validation
    @Transactional
    public Member validateMember(HttpServletRequest httpRequest) {
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
        // 2. 토큰에 맞는 회원정보를 가져온다.
        return tokenProvider.getMemberFromAuthentication();
    }

    // 게시글 존재 여부
    @Transactional(readOnly = true)
    public Post isPresentPost(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        return optionalPost.orElse(null);
    }

    // 게시글 작성
    @Transactional
    public PostResponseDto writePost(PostRequestDto postRequestDto, HttpServletRequest httpRequest) {

        // 1. token 유효성 검사 후 회원정보를 가져온다
        Member memberInfo = validateMember(httpRequest);
        if(memberInfo == null) {
            throw new IllegalArgumentException("Token값이 유효하지 않습니다.");
        }
        // 2. 게시글 저장
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .author(memberInfo.getName())
                .category(postRequestDto.getCategory())
                .member(memberInfo)
                .build();
        postRepository.save(post);

        PostResponseDto postList = PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getMember().getName())
                .category(post.getCategory())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();

        return postList;
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto postRequestDto, HttpServletRequest httpRequest) {
        // 1. token 유효성 검사 후 회원정보를 가져온다
        Member memberInfo = validateMember(httpRequest);
        if(memberInfo == null) {
            throw new IllegalArgumentException("Token값이 유효하지 않습니다.");
        }
        Post post = isPresentPost(id);
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글 id 입니다.");
        }

        if (post.validateMember(memberInfo)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        post.update(postRequestDto);

        PostResponseDto postList = PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getMember().getName())
                .category(post.getCategory())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();

        return postList;
    }

    // 게시글 삭제
    // 게시글 전체조회
    // 게시글 상세조회
    // 게시글 좋아요 리스트
    // 마이페이지) 작성한 게시글 조회 => 인증된 정보로 현재 로그인된 유저가 작성한 게시글을 조회


    //카테고리 번호로 게시글 불러오기



}
