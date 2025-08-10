package net.brekker.auth.security;

import lombok.RequiredArgsConstructor;
import net.brekker.auth.service.UserService;
import net.brekker.common.enums.ProviderType;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Lazy
    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        OAuth2Attributes.Normalized normalized = OAuth2Attributes.normalize(registrationId, oAuth2User.getAttributes());

        if (normalized.email() == null || normalized.name() == null) {
            throw new OAuth2AuthenticationException("Provider did not return email or name");
        }

        ProviderType providerType = ProviderType.valueOf(registrationId);
        userService.saveOAuthUserIfExists(normalized.email(), normalized.name(), providerType);

        return oAuth2User;
    }
}
