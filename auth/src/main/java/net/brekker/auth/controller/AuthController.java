package net.brekker.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.brekker.auth.dto.SignInRequestDto;
import net.brekker.auth.dto.SignUpRequestDto;
import net.brekker.auth.dto.UserDto;
import net.brekker.auth.security.JwtService;
import net.brekker.auth.service.AuthService;
import net.brekker.auth.service.UserService;
import net.brekker.common.annotation.RateLimited;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Контроллер аутентификации", description = "Обслуживает запрос аутентификации/авторизации")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @RateLimited
    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Регистрация прошла успешно"
            )
    })
    @PostMapping("/register")
    public void signup(HttpServletResponse response, @RequestBody @Valid SignUpRequestDto request) {
        authService.signup(response, request);
    }

    @RateLimited
    @Operation(summary = "Авторизация пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Авторизация прошла успешно"
            )
    })
    @PostMapping("/login")
    public void login(HttpServletResponse response, @RequestBody @Valid SignInRequestDto request) {
        authService.login(response, request);
    }

    @RateLimited
    @Operation(summary = "Рефреш токена авторизации")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Рефреш токена авторизации прошел успешно"
            )
    })
    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshToken(request, response);
    }

    @Operation(summary = "Выход пользователя из системы")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь вышел из системы"
            )
    })
    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        authService.logout(response);
    }

    @Operation(summary = "Получение текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Получение текущего пользователя прошел успешно"
            )
    })
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getCurrentUser(HttpServletRequest request) {
        String token = jwtService.extractTokenFromCookie(request, AuthService.JWT_ACCESS_TOKEN_COOKIE_NAME);
        if (token == null || token.isBlank() || !jwtService.validateToken(token)) {
            return null;
        }

        String username = jwtService.extractUsername(token);
        return userService.getByUsername(username);
    }
}
