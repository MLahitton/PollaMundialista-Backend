package com.mundialpolla.shared.config;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Qualifier("appJwtDecoder") JwtDecoder appJwtDecoder,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/google").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/v1/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/dataset/world-cup-2026/import").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/internal/clock").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/clock/real").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/clock/historical").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/dev/participants").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/scoring/matches/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/scoring/run").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public-predictions/**").permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/api/v1/tournaments/**",
                                "/api/v1/teams/**",
                                "/api/v1/stages/**",
                                "/api/v1/groups/**",
                                "/api/v1/matches/**",
                                "/api/v1/predictions/**",
                                "/api/v1/prediction-scores/**",
                                "/api/v1/rankings/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.decoder(appJwtDecoder))
                )
                .build();
    }
}
