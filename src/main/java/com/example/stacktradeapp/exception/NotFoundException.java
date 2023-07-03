package com.example.stacktradeapp.exception;


public class NotFoundException extends RuntimeException{

    //constructor with message parameter
    public NotFoundException(String message) {
        super(message);
    }
    //constructor with message and cause parameter
    public NotFoundException(String message, Throwable cause) {
        super(message);
    }

    //constructor with cause parameter
    public NotFoundException(Throwable cause) {
        super(cause);
    }


}
