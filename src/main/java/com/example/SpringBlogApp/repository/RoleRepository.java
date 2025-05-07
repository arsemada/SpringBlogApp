package com.example.SpringBlogApp.repository;


import com.example.SpringBlogApp.model.Role;  // Import the Role entity
import org.springframework.data.jpa.repository.JpaRepository;  // Import JpaRepository
import org.springframework.stereotype.Repository;  // Import Repository annotation

import java.util.Optional;  // Import Optional

@Repository  // Marks this interface as a Spring Data JPA repository.
public interface RoleRepository extends JpaRepository<Role, Long> {  // Extends JpaRepository, providing CRUD operations for Role entities.
    Optional<Role> findByName(String name);  // Declares a method to find a role by its name. Returns an Optional.
    boolean existsByName(String name);
}