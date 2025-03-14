package com.habittracker.habit.exception;

/**
 * Base exception class for Habit Service exceptions.
 */
public class HabitServiceException extends RuntimeException {
    
    public HabitServiceException(String message) {
        super(message);
    }
    
    public HabitServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
