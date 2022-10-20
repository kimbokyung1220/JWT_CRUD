package com.example.intermediate.controller;

import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.UserDetailsImpl;
import com.example.intermediate.service.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class LikesController {
    private final LikesService likesService;

    // 좋아요, 좋아요 취소
    @PostMapping("/{postid}/like")
    public ResponseDto<?> Likes(@PathVariable Long postid, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return likesService.likes(postid, userDetails);
    }
}
