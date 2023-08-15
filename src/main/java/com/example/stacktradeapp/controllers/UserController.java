package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.models.User;
import com.example.stacktradeapp.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Getting current user");
        return ResponseEntity.ok(userService.getCurrentUser(request,response)) ;
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user,HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Updating user");
        System.out.println("user "+ user);
        return ResponseEntity.ok(userService.updateUser(user,request,response));
    }
}
