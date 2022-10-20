package com.example.intermediate.controller;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Post;
import com.example.intermediate.service.PostService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/write")
    public PostResponseDto writePost(@RequestBody PostRequestDto postRequestDto, HttpServletRequest httpRequest) {
        return postService.writePost(postRequestDto, httpRequest);
    }

    // 게시글 수정
    @PutMapping("/edit/{postId}")
    public PostResponseDto updatePost(@PathVariable Long postId, @RequestBody PostRequestDto postRequestDto, HttpServletRequest httpRequest) {
        return postService.updatePost(postId, postRequestDto, httpRequest);
    }

    // 게시글 삭제
    @DeleteMapping("/remove/{postId}")
    public Long deletePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        return postService.deletePost(postId, httpRequest);
    }

    // 게시글 전체조회
    @GetMapping(value = "/list")
    public ResponseDto<?> getAllPosts(){
        return postService.getAllPosts();
    }

    // 게시글 상세조회
    @GetMapping(value = "/detail/{postId}")
    public ResponseDto<?> detailPost(@PathVariable Long postId) {
        return postService.getDetailPost(postId);
    }
    
    // 카테고리 변호로 게시글 가져오기
    @GetMapping(value = "/category/{id}")
    public ResponseDto<?> categoryAll(@PathVariable Long id){
        return postService.categoryAllGet(id);
    }


}
