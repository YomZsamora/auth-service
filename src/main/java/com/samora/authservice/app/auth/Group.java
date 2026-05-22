
package com.samora.authservice.app.auth;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "groups_id_seq")
    @SequenceGenerator(name = "groups_id_seq", sequenceName = "groups_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupPermission> groupPermissions;
}