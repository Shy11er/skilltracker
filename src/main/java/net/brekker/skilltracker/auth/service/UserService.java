package net.brekker.skilltracker.auth.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.db.domain.Role;
import net.brekker.skilltracker.auth.db.domain.User;
import net.brekker.skilltracker.auth.db.repository.UserRepository;
import net.brekker.skilltracker.auth.dto.UserDto;
import net.brekker.skilltracker.common.enums.RoleName;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public User get(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(String.format("User not found with id: %s", id)));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public UserDto save(UserDto dto) {
        User user = modelMapper.map(dto, User.class);

        User findUser = getByEmail(user.getEmail());
        if (nonNull(findUser)) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", user.getEmail()));
        }

        findUser = getByUsername(user.getUsername());
        if (nonNull(findUser)) {
            throw new IllegalArgumentException(String.format("User with username %s already exists", user.getUsername()));
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.getByName(RoleName.ROLE_USER));
        user.setRoles(roles);

        return modelMapper.map(userRepository.save(user), UserDto.class);
    }
}
