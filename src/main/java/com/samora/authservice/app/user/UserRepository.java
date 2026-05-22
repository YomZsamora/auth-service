
package com.samora.authservice.app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email, excluding soft-deleted records
    Optional<User> findByEmail(String email);

    // Check if an email exists, excluding soft-deleted records
    boolean existsByEmail(String email);

    // Check if a username exists, excluding soft-deleted records
    boolean existsByUsername(String username);

    // Check if a phone number exists, excluding soft-deleted records
    boolean existsByPhoneNumber(String phoneNumber);

    // Replaces findById — returns empty if soft-deleted
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // Replaces findAll — only returns active venues, sorted by createdAt desc
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

}