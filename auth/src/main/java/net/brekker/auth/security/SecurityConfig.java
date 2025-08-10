package net.brekker.auth.security;

import lombok.RequiredArgsConstructor;
import net.brekker.auth.service.CookieService;
import net.brekker.auth.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtTokenFilter jwtTokenFilter,
            UnauthorizedEntryPoint unauthorizedEntryPoint,
            CustomOAuth2UserService customOAuth2UserService,
            UserService userService,
            CookieService cookieService) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(
                        manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/auth/**",
                                                "/error",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html",
                                                "/swagger-resources/**",
                                                "/v3/api-docs/**",
                                                "/actuator/health/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler(userService, cookieService))
                )
                .exceptionHandling(
                        eh ->
                                eh.authenticationEntryPoint(unauthorizedEntryPoint)
                                        .accessDeniedHandler(new AccessDeniedHandlerImpl()))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService(UserService userService) {
        return new CustomOAuth2UserService(userService);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(UserService userService, CookieService cookieService) {
        return new OAuth2SuccessHandler(userService, cookieService);
    }
}
