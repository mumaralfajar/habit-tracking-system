package com.habittracker.habit.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<ValidationError> errors;
    
    public ErrorResponseDTO(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
