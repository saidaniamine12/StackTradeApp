package com.example.stacktradeapp.controllers;

import com.example.stacktradeapp.exception.AuthAPIException;
import com.example.stacktradeapp.models.AuthenticationRequest;
import com.example.stacktradeapp.models.AuthenticationResponse;
import com.example.stacktradeapp.models.RegisterRequest;
import com.example.stacktradeapp.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://localhost:4200",allowedHeaders = "*")
public class AuthenticationController {

    Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            logger.info("Registering user: {}", request);
            String response = service.register(request);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("response", response);

            return ResponseEntity.ok(responseBody);

        } catch (AuthAPIException e) {
            logger.error("Error while registering user: {}", e.getMessage());
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("response", e.getMessage());
            return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse httpServletResponse
    ) {

        return ResponseEntity.ok(service.authenticate(request, httpServletResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request
    ) throws IOException {
        logger.info("Refreshing token...");
        return ResponseEntity.ok(service.refreshToken(request)) ;

    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        logger.info("Logging out...");
        service.logout(request, response);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("response", "Logged out successfully");
        return ResponseEntity.ok(responseBody);
    }


}
