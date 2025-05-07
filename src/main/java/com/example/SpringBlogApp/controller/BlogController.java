package com.example.SpringBlogApp.controller;

import com.example.SpringBlogApp.dto.BlogResponse;
import com.example.SpringBlogApp.dto.CreateBlogRequest;
import com.example.SpringBlogApp.model.Blog;
import com.example.SpringBlogApp.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs() {
        List<Blog> blogs = blogService.getAllBlogs();
        List<BlogResponse> blogResponses = blogs.stream()
                .map(this::convertToBlogResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(blogResponses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        Blog blog = blogService.getBlogById(id);
        BlogResponse blogResponse = convertToBlogResponse(blog);
        return new ResponseEntity<>(blogResponse, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateBlogRequest request
    ) {
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        Blog createdBlog = blogService.createBlog(userDetails.getUsername(), blog);
        BlogResponse blogResponse = convertToBlogResponse(createdBlog);
        return new ResponseEntity<>(blogResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponse> updateBlog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CreateBlogRequest request // Reuse the CreateBlogRequest for simplicity
    ) {
        Blog updatedBlog = new Blog();
        updatedBlog.setTitle(request.getTitle());
        updatedBlog.setContent(request.getContent());
        Blog blog = blogService.updateBlog(userDetails.getUsername(), id, updatedBlog);
        BlogResponse blogResponse = convertToBlogResponse(blog);
        return new ResponseEntity<>(blogResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        blogService.deleteBlog(userDetails.getUsername(), id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BlogResponse>> getBlogsByUserId(@PathVariable Long userId) {
        List<Blog> blogs = blogService.getBlogsByUserId(userId);
        List<BlogResponse> blogResponses = blogs.stream()
                .map(this::convertToBlogResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(blogResponses, HttpStatus.OK);
    }

    private BlogResponse convertToBlogResponse(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setAuthor(blog.getUser().getUsername()); // Get the username from the User object
        response.setCreatedAt(blog.getCreatedAt());
        return response;
    }
}

