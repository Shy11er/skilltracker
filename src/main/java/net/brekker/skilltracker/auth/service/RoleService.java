package net.brekker.skilltracker.auth.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.db.domain.Role;
import net.brekker.skilltracker.auth.db.repository.RoleRepository;
import net.brekker.skilltracker.common.enums.RoleName;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getByName(RoleName name) {
        return roleRepository
                .findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Role not found with name: %s", name)));
    }
}
