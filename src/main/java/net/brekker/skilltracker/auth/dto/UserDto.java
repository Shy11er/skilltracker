package net.brekker.skilltracker.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.brekker.skilltracker.common.enums.RoleName;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
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
}
