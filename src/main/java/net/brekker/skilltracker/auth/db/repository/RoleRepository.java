package net.brekker.skilltracker.auth.db.repository;

import net.brekker.skilltracker.auth.db.domain.Role;
import net.brekker.skilltracker.common.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}
