package com.example.stacktradeapp.security.config;

import com.example.stacktradeapp.security.jwt.JwtAuthEntryPoint;

import org.springframework.context.annotation.Bean;

import org.springframework.security.authentication.AuthenticationProvider;
import com.example.stacktradeapp.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class WebAuthorizationConfig {


    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.cacheControl(cache -> cache.disable()))
                .authorizeHttpRequests( auth ->
                        auth.requestMatchers(
                                        "/api/auth/**",
                                        "/test/**",
                                        "/user/**",
                                        "/tickets/**"
                                        )
                                .permitAll()

                                .anyRequest().authenticated()

                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)


        ;
        return httpSecurity.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration corsConfig = new CorsConfiguration();
            corsConfig.setAllowCredentials(true);
            corsConfig.addAllowedOrigin("https://localhost:4200,http://localhost:4200");
            corsConfig.addAllowedHeader("Authorization,Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers");
            corsConfig.addAllowedMethod("GET,POST,PUT,DELETE,OPTIONS");
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfig);
            return source;

    }







}
