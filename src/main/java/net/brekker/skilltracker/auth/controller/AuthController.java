package net.brekker.skilltracker.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.dto.SignInRequestDto;
import net.brekker.skilltracker.auth.dto.SignUpRequestDto;
import net.brekker.skilltracker.auth.security.CustomUserDetailsService;
import net.brekker.skilltracker.auth.security.JwtService;
import net.brekker.skilltracker.auth.service.AuthService;
import net.brekker.skilltracker.common.annotation.RateLimited;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Контроллер аутентификации", description = "Обслуживает запрос аутентификации/авторизации")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @RateLimited
    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Регистрация прошла успешно"
            )
    })
    @PostMapping("/signup")
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

}
