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
        System.out.println("this is the request from get current user" );
        String email = jwtService.extractUsernameFromAuthHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
        System.out.println("this is the email from get current user" );
        System.out.println(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        System.out.println("this is the user from get current user" );
        System.out.println(user.getName());
        logger.info("User found: {}", user);
         return user;
    }
}
