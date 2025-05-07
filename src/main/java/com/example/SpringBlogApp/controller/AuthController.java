package com.example.SpringBlogApp.controller;

import com.example.SpringBlogApp.dto.AuthenticationRequest;
import com.example.SpringBlogApp.dto.RegistrationRequest;
import com.example.SpringBlogApp.model.Role;
import com.example.SpringBlogApp.model.User;
import com.example.SpringBlogApp.repository.RoleRepository;
import com.example.SpringBlogApp.repository.UserRepository;
import com.example.SpringBlogApp.service.CustomUserDetailsService;
import com.example.SpringBlogApp.util.JwtUtil;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest request) { // Use @Valid
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign a default role (e.g., "ROLE_USER")
        Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
        if (userRole.isEmpty()) {
            // Handle the case where the "ROLE_USER" role doesn't exist
            return new ResponseEntity<>("Default role not found!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        user.setRoles(List.of(userRole.get())); // Assign the role

        userRepository.save(user);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}


