package net.brekker.skilltracker.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.brekker.skilltracker.auth.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {
    private final JwtService jwtService;

    public static final String JWT_REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    public static final String JWT_ACCESS_TOKEN_COOKIE_NAME = "access-token";

    public void addAccessTokenCookie(HttpServletResponse response, UserDetails user) {
        String accessToken = jwtService.generateToken(user);
        Cookie cookie = build(JWT_ACCESS_TOKEN_COOKIE_NAME, accessToken, "/", 15 * 60);
        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, UserDetails user) {
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        Cookie cookie = build(JWT_REFRESH_TOKEN_COOKIE_NAME, refreshToken, "/auth/refresh", 7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private Cookie build(String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        return cookie;
    }
}
