package net.brekker.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;

@Schema(enumAsRef = true)
public enum RoleName implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_MENTOR,
    ROLE_USER;

    RoleName() {}

    @Override
    public String getAuthority() {
        return name();
    }
}