package net.brekker.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на аутентификацию")
public class SignUpRequestDto {
    @Schema(description = "Логин пользователя")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @Schema(description = "Email пользователя")
    @Size(min = 3, max = 50, message = "Email должно содержать от 3 до 50 символов")
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректная электронная почта")
    private String email;

    @Schema(description = "Пароль")
    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
