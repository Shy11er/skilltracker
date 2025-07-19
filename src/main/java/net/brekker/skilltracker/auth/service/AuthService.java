package net.brekker.skilltracker.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.dto.SignInRequestDto;
import net.brekker.skilltracker.auth.dto.SignUpRequestDto;
import net.brekker.skilltracker.auth.dto.UserDto;
import net.brekker.skilltracker.auth.security.CustomUserDetailsService;
import net.brekker.skilltracker.auth.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    public void signup(HttpServletResponse response, SignUpRequestDto signUpRequestDto) {
        UserDto userDto = modelMapper.map(signUpRequestDto, UserDto.class);
        UserDto savedUser = userService.save(userDto);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities(savedUser.getRoles())
                .build();

        setAccessTokenOnly(response, userDetails);
    }

    public void login(HttpServletResponse response, SignInRequestDto signInRequestDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequestDto.getUsername(), signInRequestDto.getPassword()));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(signInRequestDto.getUsername());

        setAuthCookies(response, userDetails);
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

    private void setAccessTokenOnly(HttpServletResponse response, UserDetails user) {
        cookieService.addAccessTokenCookie(response, user);
    }

    private void setAuthCookies(HttpServletResponse response, UserDetails user) {
        cookieService.addAccessTokenCookie(response, user);
        cookieService.addRefreshTokenCookie(response, user);
    }
}
