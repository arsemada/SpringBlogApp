package com.example.SpringBlogApp.repository;

import com.example.SpringBlogApp.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUser_Id(Long userId); // Find blogs by user ID
    List<Blog> findAllByOrderByCreatedAtDesc(); // get all blogs by order created at
}

