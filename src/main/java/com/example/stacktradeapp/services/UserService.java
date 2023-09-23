package com.example.stacktradeapp.services;

import com.example.stacktradeapp.exception.JwtAuthenticationException;
import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.models.jiraServerExtractedEntities.JiraServerTicket;
import com.example.stacktradeapp.repositories.UserRepository;
import com.example.stacktradeapp.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getCurrentUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public User updateUser(User updatedUser) {
        User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (updatedUser.getName() != null) {
            userDetails.setName(updatedUser.getName());
        }
        if (updatedUser.getLocation() != null) {
            userDetails.setLocation(updatedUser.getLocation());
        }
        if (updatedUser.getCompanyName() != null) {
            userDetails.setCompanyName(updatedUser.getCompanyName());
        }
        return userRepository.save(userDetails);
    }

}
