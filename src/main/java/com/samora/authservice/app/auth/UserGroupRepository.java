
package com.samora.authservice.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    // Find all UserGroup associations for a given user ID
    List<UserGroup> findAllByUserId(Long userId);

    // Find all UserGroup associations for a given group ID
    List<UserGroup> findAllByGroupId(Long groupId);
}