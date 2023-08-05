package com.example.stacktradeapp.services;


import com.example.stacktradeapp.enums.Role;
import com.example.stacktradeapp.enums.TokenType;
import com.example.stacktradeapp.exception.AuthAPIException;
import com.example.stacktradeapp.models.*;
import com.example.stacktradeapp.repositories.TokenRepository;
import com.example.stacktradeapp.repositories.UserRepository;
import com.example.stacktradeapp.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import jakarta.servlet.http.Cookie;
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Email already exists!.");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        logger.info("User created: {}", user);


        userRepository.save(user);
        return "User registered successfully!.";
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse httpServletResponse) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        logger.info("User authenticated: {}", user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        //add refresh token in cookie
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setDomain("localhost");
        refreshCookie.setSecure(true);
        httpServletResponse.addCookie(refreshCookie);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponse refreshToken(
            HttpServletRequest request
    ) throws IOException {

        String refreshToken = null;
        final String userEmail;
        for (Cookie cookie : request.getCookies()) {
             if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
                break;
             }
        }
        if (refreshToken == null) {
            return null;
        }

        userEmail = jwtService.extractUsernameFromToken(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build();
            }
        }
        throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Invalid token!.");
    }

    public User getCurrentUser(HttpServletRequest request) throws IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Invalid token!.");
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsernameFromToken(refreshToken);
        if (userEmail != null) {
            return this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
        }
        throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Invalid token!.");
    }
}

