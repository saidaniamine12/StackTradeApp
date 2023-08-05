package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "https://localhost:4200",allowedHeaders = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/current")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getCurrentUser(request)) ;
    }
}
