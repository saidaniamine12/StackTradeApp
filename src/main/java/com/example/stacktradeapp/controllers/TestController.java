package com.example.stacktradeapp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.stacktradeapp.services.UserService;
@RequestMapping("/test")
@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        System.out.println("this is the request");
        System.out.println(request);
        System.out.println(userService.getCurrentUser(request));
        System.out.println("this is the user");
        return "Hello World";
    }
}
