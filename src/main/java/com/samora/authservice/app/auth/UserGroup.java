
package com.samora.authservice.app.auth;

import com.samora.authservice.app.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_groups",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_groups_id_seq")
    @SequenceGenerator(name = "user_groups_id_seq", sequenceName = "user_groups_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
}