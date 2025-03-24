package com.habittracker.habit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor for resource not found with resource details
     * 
     * @param resourceType the type of resource (e.g., "Habit", "Category")
     * @param fieldName the field name used to search (e.g., "id")
     * @param fieldValue the value that was searched for
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceType, fieldName, fieldValue));
    }
}
