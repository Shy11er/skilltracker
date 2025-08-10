package net.brekker.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.brekker.auth.dto.UserDto;
import net.brekker.auth.service.CookieService;
import net.brekker.auth.service.UserService;
import net.brekker.common.enums.ProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final CookieService cookieService;

    @Value("${application.url}")
    private String applicationUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = (authentication instanceof OAuth2AuthenticationToken oAuth2Token)
                ? oAuth2Token.getAuthorizedClientRegistrationId()
                : null;

        if (registrationId == null) {
            throw new OAuth2AuthenticationException("Unknown OAuth2 provider");
        }

        ProviderType provider = ProviderType.valueOf(registrationId.toUpperCase());

        OAuth2Attributes.Normalized normalized =
                OAuth2Attributes.normalize(registrationId, oAuth2User.getAttributes());

        if (normalized.email() == null || normalized.email().isBlank()
                || normalized.name() == null || normalized.name().isBlank()) {
            throw new OAuth2AuthenticationException("Provider did not return required attributes (email/name)");
        }

        UserDto userDto = userService.saveOAuthUserIfExists(normalized.email(), normalized.name(), provider);
        UserDetails userDetails = userDto.toUserDetails();

        cookieService.addAccessTokenCookie(response, userDetails);
        cookieService.addRefreshTokenCookie(response, userDetails);

        String redirectUrl = String.format("%s/oauth2/callback", applicationUrl);

        response.sendRedirect(redirectUrl);
    }
}
