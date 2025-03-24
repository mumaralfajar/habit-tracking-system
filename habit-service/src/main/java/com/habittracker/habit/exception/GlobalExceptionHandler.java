package com.habittracker.habit.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.habittracker.habit.exception.dto.ErrorResponseDTO;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for the habit service.
 * Centralizes exception handling across all controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles ResourceNotFoundException by returning a 404 NOT FOUND response.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handles the standard EntityNotFoundException, converting it to our ResourceNotFoundException.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        
        log.error("Entity not found: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidRequestException by returning a 400 BAD REQUEST response.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRequestException(
            InvalidRequestException ex, WebRequest request) {
        
        log.error("Invalid request: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other HabitServiceExceptions by returning a 500 INTERNAL SERVER ERROR response.
     */
    @ExceptionHandler(HabitServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleHabitServiceException(
            HabitServiceException ex, WebRequest request) {
        
        log.error("Habit service error: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles all other exceptions by returning a 500 INTERNAL SERVER ERROR response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors by returning a 400 BAD REQUEST response with validation details.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
            
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Maps FieldError to ValidationError
     */
    private ErrorResponseDTO.ValidationError mapFieldError(FieldError fieldError) {
        return new ErrorResponseDTO.ValidationError(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }
    
    /**
     * Extracts the request path from the WebRequest
     */
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return "";
    }
}
