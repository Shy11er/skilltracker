package net.brekker.skilltracker.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.db.domain.User;
import net.brekker.skilltracker.auth.dto.SignInRequestDto;
import net.brekker.skilltracker.auth.dto.SignUpRequestDto;
import net.brekker.skilltracker.auth.dto.UserDto;
import net.brekker.skilltracker.auth.security.CustomUserDetailsService;
import net.brekker.skilltracker.auth.security.JwtService;
import net.brekker.skilltracker.common.enums.ProviderType;
import net.brekker.skilltracker.common.exceptions.ProviderConflictException;
import net.brekker.skilltracker.common.utils.RandomPasswordUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import com.google.api.client.json.jackson2.JacksonFactory;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String JWT_REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    public static final String JWT_ACCESS_TOKEN_COOKIE_NAME = "access-token";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final CookieService cookieService;


    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public void signup(HttpServletResponse response, SignUpRequestDto signUpRequestDto) {
        UserDto userDto = modelMapper.map(signUpRequestDto, UserDto.class);
        UserDto savedUser = userService.save(userDto, ProviderType.LOCAL);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities(savedUser.getRoles())
                .build();

        setAccessTokenOnly(response, userDetails);
    }

    public void login(HttpServletResponse response, SignInRequestDto signInRequestDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequestDto.getUsername(),
                            signInRequestDto.getPassword())
            );
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(signInRequestDto.getUsername());
            setAuthCookies(response, userDetails);
        } catch (AuthenticationException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtService.extractTokenFromCookie(request, JWT_REFRESH_TOKEN_COOKIE_NAME);

        if (!jwtService.validateToken(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        setAccessTokenOnly(response, userDetails);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(JWT_ACCESS_TOKEN_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void googleLogin(String token, HttpServletResponse response) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(clientId)).build();
        try {
            GoogleIdToken idToken = verifier.verify(token);

            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            UserDto existingUser = userService.getByEmail(email);
            UserDetails userDetails;

            if (nonNull(existingUser)) {
                if (!existingUser.getProvider().equals(ProviderType.GOOGLE)) {
                    throw new ProviderConflictException("Account with same email already registered with the different way");
                }

                userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(existingUser.getUsername())
                        .password(existingUser.getPassword())
                        .authorities(existingUser.getRoles())
                        .build();
            } else {
                String name = payload.get("name").toString();
                UserDto newUser = UserDto.builder()
                        .email(email)
                        .username(name)
                        .password(RandomPasswordUtil.generate(10))
                        .provider(ProviderType.GOOGLE)
                        .build();

                UserDto savedUser = userService.save(newUser, ProviderType.GOOGLE);

                userDetails = org.springframework.security.core.userdetails.User
                        .withUsername(savedUser.getUsername())
                        .password(savedUser.getPassword())
                        .authorities(savedUser.getRoles())
                        .build();
            }

            setAuthCookies(response, userDetails);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to verify Google token", e);
        }
    }

    private void setAccessTokenOnly(HttpServletResponse response, UserDetails user) {
        cookieService.addAccessTokenCookie(response, user);
    }

    private void setAuthCookies(HttpServletResponse response, UserDetails user) {
        cookieService.addAccessTokenCookie(response, user);
        cookieService.addRefreshTokenCookie(response, user);
    }
}
