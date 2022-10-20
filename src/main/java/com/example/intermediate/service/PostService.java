package com.example.intermediate.service;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.CommentResponseDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Category;
import com.example.intermediate.domain.Comment;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.CommentRepository;
import com.example.intermediate.repository.LikesRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;

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
    public Post isPresentPost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
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
                .postId(post.getPostId())
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
    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto, HttpServletRequest httpRequest) {
        // 1. token 유효성 검사 후 회원정보를 가져온다
        Member memberInfo = validateMember(httpRequest);
        if(memberInfo == null) {
            throw new IllegalArgumentException("Token값이 유효하지 않습니다.");
        }
        Post post = isPresentPost(postId);
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글 id 입니다.");
        }

        if (post.validateMember(memberInfo)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }
        // 카테고리를 빼고 글을 수정하면 카테고리 정보 유지
        if(postRequestDto.getCategory() == null) {
            postRequestDto.setCategory(post.getCategory());
        }
        post.update(postRequestDto);

        PostResponseDto postList = PostResponseDto.builder()
                .postId(post.getPostId())
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
    @Transactional
    public Long deletePost(Long postId, HttpServletRequest httpRequest){

        Member memberInfo = validateMember(httpRequest);
        if(memberInfo == null) {
            throw new IllegalArgumentException("Token값이 유효하지 않습니다.");
        }

        Post postInfo = isPresentPost(postId);
        if(postInfo == null) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }

        if (postInfo.validateMember(memberInfo)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(postInfo);
        return postInfo.getPostId();
    }

    // 게시글 전체조회
    @Transactional(readOnly = true)
    public ResponseDto<?> getAllPosts() {
        return ResponseDto.success(postRepository.findAll());
    }

    // 게시글 상세조회
    @Transactional(readOnly = true)
    public ResponseDto<?> getDetailPost(Long postId) {
        Post postInfo = isPresentPost(postId);
        //select * from post where post_id = 1;
        // name, email. id= ***

        if(postInfo == null) {
            throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
        }
        // 좋아요 갯수
        long likesCount = likesRepository.countByPost(postInfo);
        // 댓글목록
        List<Comment> commentList = commentRepository.findAllByPost(postInfo);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        // findByresponseTo(commentID)

        for (Comment comment : commentList) {
            commentResponseDtoList.add(
                    CommentResponseDto.builder()
                            .commentId(comment.getCommentId())
                            .author(comment.getMember().getName())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .parentCommentId(comment.getParentCommentId())
                            .modifiedAt(comment.getModifiedAt())
                            .build()
            );
        }

        PostResponseDto postList = PostResponseDto.builder()
                .postId(postInfo.getPostId())
                .title(postInfo.getTitle())
                .content(postInfo.getContent())
                .author(postInfo.getAuthor())
                .category(postInfo.getCategory())
                .createdAt(postInfo.getCreatedAt())
                .modifiedAt(postInfo.getModifiedAt())
                .likesCount(likesCount)
                .commentResponseDtoList(commentResponseDtoList)
                .build();

        return ResponseDto.success(postList);
    }

    // 카테고리 변호로 게시글 가져오기
    @Transactional
    public ResponseDto<?> categoryAllGet(Long id){
        Category categoryInfo = Category.findById(id);
        return ResponseDto.success(postRepository.findByCategory(categoryInfo));
    }

    // 게시글 좋아요 리스트


    // 마이페이지) 작성한 게시글 조회 => 인증된 정보로 현재 로그인된 유저가 작성한 게시글을 조회


    //카테고리 번호로 게시글 불러오기



}
