package net.brekker.skilltracker.auth.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import net.brekker.skilltracker.common.enums.RoleName;

import java.util.UUID;

@Getter
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name="name", nullable = false, length = 50, unique = true)
    private RoleName name;
}
