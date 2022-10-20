package com.example.intermediate.repository;

import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.domain.Category;
import com.example.intermediate.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long postId);
    List<Post> findByCategory(Category category);

}
