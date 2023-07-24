package com.example.stacktradeapp.exception;

public class DocumentParsingException extends Exception{
    public DocumentParsingException(String message) {
        super(message);
    }

    public DocumentParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
