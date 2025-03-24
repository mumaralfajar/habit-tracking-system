package com.habittracker.habit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a client sends an invalid request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
