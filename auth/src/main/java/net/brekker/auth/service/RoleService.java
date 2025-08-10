package net.brekker.auth.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.brekker.auth.db.domain.Role;
import net.brekker.auth.db.repository.RoleRepository;
import net.brekker.common.enums.RoleName;
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
