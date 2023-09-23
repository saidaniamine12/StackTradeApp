package com.example.stacktradeapp.services;


import com.example.stacktradeapp.enums.Role;
import com.example.stacktradeapp.enums.TokenType;
import com.example.stacktradeapp.exception.AuthAPIException;
import com.example.stacktradeapp.exception.JwtAuthenticationException;
import com.example.stacktradeapp.exception.NotFoundException;
import com.example.stacktradeapp.models.*;
import com.example.stacktradeapp.repositories.ConfirmationTokenRepository;
import com.example.stacktradeapp.repositories.TokenRepository;
import com.example.stacktradeapp.repositories.UserRepository;
import com.example.stacktradeapp.security.jwt.JwtAuthEntryPoint;
import com.example.stacktradeapp.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;

    public String register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())){
            throw new AuthAPIException(HttpStatus.BAD_REQUEST, "Email already exists!.");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(false)
                .build();
        logger.info("User created: {}", user);
        userRepository.save(user);


        String token = UUID.randomUUID().toString();
        //create a confirmation Token
        ConfirmationToken confirmationToken = ConfirmationToken.builder()
                .user(user)
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .confirmedAt(null)
                .build();
        ConfirmationToken savedToken = saveConfirmationToken(confirmationToken);
        String link = "http://localhost:8443/api/auth/confirm?token=";
        EmailDetails emailDetails = EmailDetails.builder()
                .msgBody("please click on the link to enable your account. "+ link + savedToken.getToken())
                .subject("Confirm your email")
                .recipient(user.getEmail())
                .build();
        emailService.send(emailDetails);
        logger.info("Confirmation email sent to: {}", user.getEmail());

        return "User registered successfully!.";
    }


    public void confirmTicket(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByConfirmationToken(token).orElse(null);
        if (confirmationToken == null) {
            throw new NotFoundException("Token not found!.");
        }
        if (confirmationToken.getConfirmedAt() != null) {
            String msg = String.format("Email %s already confirmed!.", confirmationToken.getUser().getEmail());
            throw new AuthAPIException(msg);
        }
        User user = confirmationToken.getUser();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        //confirmationTokenRepository.updateConfirmedAt(confirmationToken.getId(),LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
        user.setEnabled(true);
        user.setLocked(false);
        userRepository.save(user);
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
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
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
//        validUserTokens.forEach(token -> {
//            token.setExpired(true);
//            token.setRevoked(true);
//        });
//        tokenRepository.saveAll(validUserTokens);
        tokenRepository.deleteAll(validUserTokens);
    }

    public AuthenticationResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        String refreshToken = null;
        final String userEmail;
        try {
            if(request.getCookies() == null){
                throw new JwtAuthenticationException("no cookies found");
            }
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();

                }
            }
            if (refreshToken == null) {
                throw new JwtAuthenticationException("refresh token not found");

            } else {
                if (refreshToken.equals("")) {
                    throw new JwtAuthenticationException("refresh token is empty");
                }
            }

            userEmail = jwtService.extractUsernameFromToken(refreshToken);
            if (userEmail != null) {
                var user = this.userRepository.findByEmail(userEmail)
                        .orElseThrow();
                if (jwtService.isTokenValid(refreshToken, user)) {
                    var accessToken = jwtService.generateToken(user);
                    System.out.println("access token : " + accessToken);
                    revokeAllUserTokens(user);
                    saveUserToken(user, accessToken);
                    return AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .build();

                }
            }
        }
        catch (JwtAuthenticationException e){
            jwtAuthEntryPoint.commence(request, response, e);
        }
        return null;
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

    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        Cookie refreshCookie = new Cookie("refreshToken", "");
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            //add refresh token in cookie
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setDomain("localhost");
            refreshCookie.setMaxAge(0);
            refreshCookie.setSecure(true);
            response.addCookie(refreshCookie);
            return ;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }

        //add refresh token in cookie
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setDomain("localhost");
        refreshCookie.setMaxAge(0);
        refreshCookie.setSecure(true);

        response.addCookie(refreshCookie);
    }

    public ConfirmationToken saveConfirmationToken(ConfirmationToken confirmationToken) {
        User user = new User();
        List<ConfirmationToken> validUserTokens = confirmationTokenRepository.findAllValidTokenByUser(user.getId());
        if (!validUserTokens.isEmpty()){
            validUserTokens.forEach(token -> {
                token.setRevoked(true);
            });
        }
        confirmationTokenRepository.saveAll(validUserTokens);

        return confirmationTokenRepository.save(confirmationToken);
    }




}

