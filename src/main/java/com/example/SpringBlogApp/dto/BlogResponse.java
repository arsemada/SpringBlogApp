package com.example.SpringBlogApp.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String author; // Username of the author
    private LocalDateTime createdAt;
}