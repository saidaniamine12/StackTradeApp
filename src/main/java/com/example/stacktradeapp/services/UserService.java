package com.example.stacktradeapp.services;

import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.repositories.UserRepository;
import com.example.stacktradeapp.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }


    public User getCurrentUser(HttpServletRequest request) {
        String email = jwtService.extractUsernameFromAuthHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
        return userRepository.findByEmail(email).orElseThrow();
    }
}
