
package com.samora.authservice.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {

    // Find all GroupPermission associations for a given group ID
    List<GroupPermission> findAllByGroupIdIn(List<Long> groupIds);
}