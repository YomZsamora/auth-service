
package com.samora.authservice.app.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "group_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "permission_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_permissions_id_seq")
    @SequenceGenerator(name = "group_permissions_id_seq", sequenceName = "group_permissions_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}