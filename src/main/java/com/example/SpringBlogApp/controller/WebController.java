package com.example.SpringBlogApp.controller;

import com.example.SpringBlogApp.dto.AuthenticationRequest;
import com.example.SpringBlogApp.dto.BlogResponse;
import com.example.SpringBlogApp.dto.CreateBlogRequest;
import com.example.SpringBlogApp.dto.RegistrationRequest;
import com.example.SpringBlogApp.model.Blog;
import com.example.SpringBlogApp.model.Role;
import com.example.SpringBlogApp.model.User;
import com.example.SpringBlogApp.repository.RoleRepository;
import com.example.SpringBlogApp.repository.UserRepository;
import com.example.SpringBlogApp.service.BlogService;
import com.example.SpringBlogApp.service.CustomUserDetailsService;
import com.example.SpringBlogApp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BlogService blogService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Welcome to My Blog");
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }



    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Register");
            return "register";
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            model.addAttribute("title", "Register");
            model.addAttribute("error", "Username is already taken!");
            return "register";
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign a default role (e.g., "ROLE_USER")
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        if (userRole.isEmpty()) {
            model.addAttribute("title", "Register");
            model.addAttribute("error", "Default role not found!");
            return "register";
        }
        user.setRoles(List.of(userRole.get())); // Assign the role

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "User registered successfully! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("title", "Login");
        model.addAttribute("authenticationRequest", new AuthenticationRequest());
        return "login";
    }

    @PostMapping("/login")
    public String authenticateUser(@ModelAttribute("authenticationRequest") AuthenticationRequest request, Model model, HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication); //Set the Authentication object to the SecurityContext

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);
            //  return ResponseEntity.ok(Map.of("token", jwt));  // changed
            httpServletRequest.getSession().setAttribute("token", jwt);
            return "redirect:/blogs";

        } catch (Exception e) {
            model.addAttribute("title", "Login");
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }

    @GetMapping("/blogs")
    public String listBlogs(Model model, HttpServletRequest request) {
        String token = (String) request.getSession().getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        if (jwtUtil.isTokenExpired(token)) {
            return "redirect:/login";
        }
        List<Blog> blogs = blogService.getAllBlogs();
        List<BlogResponse> blogResponses = blogs.stream()
                .map(this::convertToBlogResponse)
                .collect(Collectors.toList());
        model.addAttribute("title", "Blogs");
        model.addAttribute("blogs", blogResponses);
        model.addAttribute("createBlogRequest", new CreateBlogRequest());
        return "blogs";
    }

    @PostMapping("/blogs")
    public String createBlog(@ModelAttribute("createBlogRequest") CreateBlogRequest request, HttpServletRequest httpServletRequest) {
        String token = (String) httpServletRequest.getSession().getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        String username = jwtUtil.getUsernameFromToken(token);
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blogService.createBlog(username, blog);
        return "redirect:/blogs";
    }

    @GetMapping("/blogs/edit/{id}")
    public String showEditBlogForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        String token = (String) request.getSession().getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        if (jwtUtil.isTokenExpired(token)) {
            return "redirect:/login";
        }
        Blog blog = blogService.getBlogById(id);
        String username = jwtUtil.getUsernameFromToken(token);
        if (!blog.getUser().getUsername().equals(username)) {
            return "redirect:/blogs"; // Or show an error message
        }

        CreateBlogRequest editRequest = new CreateBlogRequest();
        editRequest.setTitle(blog.getTitle());
        editRequest.setContent(blog.getContent());
        model.addAttribute("title", "Edit Blog");
        model.addAttribute("blogId", id);  // Pass the blog ID to the form
        model.addAttribute("editBlogRequest", editRequest);
        return "edit_blog"; //  Create a new HTML template: edit_blog.html
    }

    @PostMapping("/blogs/edit/{id}")
    public String updateBlog(@PathVariable Long id, @ModelAttribute("editBlogRequest") CreateBlogRequest request, HttpServletRequest httpServletRequest) {
        String token = (String) httpServletRequest.getSession().getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        String username = jwtUtil.getUsernameFromToken(token);
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blogService.updateBlog(username, id, blog);
        return "redirect:/blogs";
    }

    @PostMapping("/blogs/delete/{id}")
    public String deleteBlog(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        String token = (String) httpServletRequest.getSession().getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }
        String username = jwtUtil.getUsernameFromToken(token);
        blogService.deleteBlog(username, id);
        return "redirect:/blogs";
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

