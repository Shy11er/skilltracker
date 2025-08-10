package net.brekker.auth.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import net.brekker.auth.db.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Component
public class JwtService {

    @NotNull(message = "Секретный токен не может быть пустым")
    @Value("${jwt.secret}")
    private String secret;

    @NotNull(message = "Секретный токен не может быть пустым")
    @Value("${jwt.expiration}")
    private Long expiration;

    public String extractTokenFromCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("id", customUserDetails.getId());
            claims.put("username", customUserDetails.getUsername());
            claims.put("roles", customUserDetails.getRoles());
        }
        return generateToken(claims, userDetails);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSigningKey()).compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofDays(7).toMillis());
        SecretKey key = getSigningKey();

        return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiry).signWith(key,
                SignatureAlgorithm.HS256).compact();
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        return (nonNull(authHeader) && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Получение ключа для подписи токена
     * <p>
     * Для генерации jwtSigningKey
     * SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
     * String secretString = Encoders.BASE64.encode(key.getEncoded());
     *
     * @return ключ
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
