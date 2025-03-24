package com.habittracker.habit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class HabitServiceException extends RuntimeException {
    
    public HabitServiceException(String message) {
        super(message);
    }
    
    public HabitServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
