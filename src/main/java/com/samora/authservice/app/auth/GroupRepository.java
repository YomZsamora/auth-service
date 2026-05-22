
package com.samora.authservice.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Find a group by its name, excluding soft-deleted records
    Optional<Group> findByName(String name);

    // Check if a group name exists, excluding soft-deleted records
    boolean existsByName(String name);

}