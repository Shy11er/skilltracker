package net.brekker.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import net.brekker.common.enums.ProviderType;
import net.brekker.common.enums.RoleName;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @Schema(description = "Id сущности", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @NotBlank(message = "Email не должен быть пустым")
    @Schema(description = "Email")
    private String email;

    @JsonIgnore
    @NotBlank(message = "Пароль не должен быть пустым")
    @Schema(description = "Пароль")
    private String password;

    @Schema(description = "Роли пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RoleName> roles;

    @JsonIgnore
    @Schema(description = "Провайдер")
    private ProviderType provider;

    public UserDetails toUserDetails() {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .authorities(roles.stream().map(Enum::name).toArray(String[]::new))
                .build();
    }
}
