package com.habitsystem.user.config;

import com.habitsystem.user.exception.GrpcException;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Slf4j
@GrpcGlobalServerInterceptor
public class GrpcErrorInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (GrpcException e) {
                    call.close(e.getStatus(), new Metadata());
                } catch (Exception e) {
                    call.close(Status.INTERNAL.withDescription(e.getMessage()), new Metadata());
                    log.error("Unhandled error", e);
                }
            }
        };
    }
}
