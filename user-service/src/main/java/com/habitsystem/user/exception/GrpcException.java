package com.habitsystem.user.exception;

import io.grpc.Status;
import lombok.Getter;

@Getter
public class GrpcException extends RuntimeException {
    private final Status status;
    
    public GrpcException(Status status, String message) {
        super(message);
        this.status = status;
    }
    
    public GrpcException(Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
