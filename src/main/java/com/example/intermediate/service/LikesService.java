package com.example.intermediate.service;

import com.example.intermediate.controller.response.MemberResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Likes;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.domain.UserDetailsImpl;
import com.example.intermediate.repository.LikesRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final PostService postService;
    private final LikesRepository likesRepository;

    public ResponseDto<?> likes (Long postId , UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Post post = postService.isPresentPost(postId);

        // memberId와 postId가 존재하는지 확인
        Optional<Likes> likes = likesRepository.findByMemberAndPost(member, post);

        // 1.좋아요 기록이 있으면 지워준다.
        if (likes.isPresent()) {
            likesRepository.deleteById(likes.get().getId());
            return ResponseDto.success(false);
        } else {
            // 2.좋아요 기록이 없으면 저장한다.
            Likes likesList = Likes.builder()
                    .post(post)
                    .member(member)
                    .build();
            likesRepository.save(likesList);
            return ResponseDto.success(true);
        }
    }

}
