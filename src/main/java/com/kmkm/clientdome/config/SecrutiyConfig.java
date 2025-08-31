package com.kmkm.clientdome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecrutiyConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                // Login/Session management
                "/loginsignup",
                "/api/validate-session",

                // Main chat page assets
                "/stylesheet.css",
                "/chat.js",
                "/upload-handler.js",
                "/result-stylesheet.css",
                "/result-handler.js",

                "/js/**",
                "/images/**"
            ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }
}