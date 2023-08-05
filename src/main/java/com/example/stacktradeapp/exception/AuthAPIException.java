package com.example.stacktradeapp.exception;

import org.springframework.http.HttpStatus;

public class AuthAPIException extends RuntimeException{

    private HttpStatus status;
    private String message;

    public AuthAPIException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }
    public AuthAPIException(String message) {
        super(message);
    }

    public String getMessage() {
        return message;
    }

}
