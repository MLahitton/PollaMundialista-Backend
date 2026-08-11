package com.mundialpolla.shared.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/dataset/world-cup-2026/import").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/internal/clock").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/clock/real").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/clock/historical").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/dev/participants").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/internal/scoring/matches/*").permitAll()
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
                                "/api/v1/prediction-scores/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
