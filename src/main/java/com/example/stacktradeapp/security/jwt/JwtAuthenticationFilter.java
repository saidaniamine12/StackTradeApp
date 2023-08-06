package com.example.stacktradeapp.security.jwt;

import com.example.stacktradeapp.exception.JwtAuthenticationException;
import com.example.stacktradeapp.repositories.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("Request path: {}", request.getServletPath());
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        System.out.println("authHeader: " );
        final String userEmail;
        System.out.println(request.getHeader(HttpHeaders.AUTHORIZATION));
        System.out.println("authHeader: " + authHeader);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            System.out.println("authHeader is null or does not start with Bearer");
            JwtAuthenticationException jwtAuthenticationException = new JwtAuthenticationException("authHeader is null or does not start with Bearer");
            jwtAuthEntryPoint.commence(request, response, jwtAuthenticationException);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsernameFromAuthHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            var isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);
            System.out.println("isTokenValid: " + isTokenValid);
            System.out.println(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                System.out.println("authToken: " + authToken);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("SecurityContextHolder.getContext().getAuthentication(): " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("Invalid token");
                JwtAuthenticationException jwtAuthenticationException = new JwtAuthenticationException("Invalid token");
                jwtAuthEntryPoint.commence(request, response, jwtAuthenticationException);

            }
        } else {
            System.out.println("Invalid token");
            JwtAuthenticationException jwtAuthenticationException = new JwtAuthenticationException("Invalid token");
            jwtAuthEntryPoint.commence(request, response, jwtAuthenticationException);
        }

        filterChain.doFilter(request, response);
    }
}