package net.brekker.skilltracker.auth.db.repository;

import net.brekker.skilltracker.auth.db.domain.User;
import net.brekker.skilltracker.common.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndProvider(String email, ProviderType provider);
}
