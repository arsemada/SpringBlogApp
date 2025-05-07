package com.example.SpringBlogApp.service;

import com.example.SpringBlogApp.model.Blog;
import com.example.SpringBlogApp.model.User;
import com.example.SpringBlogApp.repository.BlogRepository;
import com.example.SpringBlogApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Blog> getAllBlogs() {
        return blogRepository.findAllByOrderByCreatedAtDesc();
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    }

    public Blog createBlog(String username, Blog blog) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        blog.setUser(user); // Set the user for the blog
        return blogRepository.save(blog);
    }

    public Blog updateBlog(String username, Long id, Blog updatedBlog) {
        Blog existingBlog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        // Only allow the user who created the blog to update it
        if (!existingBlog.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this blog");
        }

        existingBlog.setTitle(updatedBlog.getTitle());
        existingBlog.setContent(updatedBlog.getContent());
        return blogRepository.save(existingBlog);
    }

    public void deleteBlog(String username, Long id) {
        Blog existingBlog = blogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

        // Only allow the user who created the blog to delete it
        if (!existingBlog.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this blog");
        }
        blogRepository.deleteById(id);
    }

    public List<Blog> getBlogsByUserId(Long userId) {
        return blogRepository.findByUser_Id(userId);
    }
}
