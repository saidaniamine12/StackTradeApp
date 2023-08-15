package com.example.stacktradeapp.services;

import com.example.stacktradeapp.exception.JwtAuthenticationException;
import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.repositories.UserRepository;
import com.example.stacktradeapp.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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


    public User getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        String email = jwtService.extractUsernameFromAuthHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
        User user = userRepository.findByEmail(email).orElseThrow();

        return userRepository.findByEmail(email).orElseThrow();
    }

    public User updateUser(User updatedUser,HttpServletRequest request, HttpServletResponse response) {
        String email = jwtService.extractUsernameFromAuthHeader(request.getHeader(HttpHeaders.AUTHORIZATION));
        User user = userRepository.findByEmail(email).orElseThrow();
        System.out.println("Updating user");
        System.out.println(updatedUser.getLocation());
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getLocation() != null) {
            user.setLocation(updatedUser.getLocation());
        }
        if (updatedUser.getCompanyName() != null) {
            user.setCompanyName(updatedUser.getCompanyName());
        }
        return userRepository.save(user);
    }
}
