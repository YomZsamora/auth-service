
package com.samora.authservice.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Find a permission by its code name, excluding soft-deleted records
    Optional<Permission> findByCodeName(String codeName);

    // Check if a permission code name exists, excluding soft-deleted records
    List<Permission> findAllByIdIn(List<Long> ids);
}