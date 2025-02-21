package com.habitsystem.user.exception;

import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

@GrpcAdvice
public class GlobalGrpcExceptionHandler {

    @GrpcExceptionHandler(GrpcException.class)
    public Status handleGrpcException(GrpcException e) {
        return e.getStatus();
    }

    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public Status handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return Status.ALREADY_EXISTS.withDescription("Resource already exists").withCause(e);
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleGenericException(Exception e) {
        return Status.INTERNAL.withDescription("Internal server error").withCause(e);
    }
}
