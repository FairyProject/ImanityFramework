package org.imanity.framework.http.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.imanity.framework.Autowired;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.http.entity.HttpHeaders;
import org.imanity.framework.http.entity.ResponseEntity;
import org.imanity.framework.http.exception.ErrorResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class FullHttpResponseFactory {

    @Autowired
    private static FrameworkBootable BOOTABLE;

    public static FullHttpResponse getSuccessResponse(Method targetMethod, List<Object> targetMethodParams, Object targetObject) {

        if (targetMethod.getReturnType() == void.class) {
            try {
                targetMethod.invoke(targetObject, targetMethodParams.toArray());
                return buildSuccessResponse();
            } catch (IllegalAccessException | InvocationTargetException e) {
                BOOTABLE.handleError(e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                Object result = targetMethod.invoke(targetObject, targetMethodParams.toArray());
                return buildSuccessResponse(result);
            } catch (IllegalAccessException | InvocationTargetException e) {
                BOOTABLE.handleError(e);
                throw new RuntimeException(e);
            }
        }
    }

    public static FullHttpResponse getErrorResponse(String url, String message, HttpResponseStatus httpResponseStatus) {
        ErrorResponse errorResponse = new ErrorResponse(httpResponseStatus.code(), httpResponseStatus.reasonPhrase(), message, url);
        byte[] content = toContentBytes(errorResponse);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
        response.headers().setInt(HttpHeaders.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    private static FullHttpResponse buildSuccessResponse(Object o) {
        if (o instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) o;
            Object contentObject = responseEntity.getBody();

            byte[] content = toContentBytes(contentObject);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, responseEntity.getStatusCode(), Unpooled.wrappedBuffer(content));
            responseEntity.getHeaders().writeToNative(response.headers());

            if (!response.headers().contains(HttpHeaders.CONTENT_TYPE)) {
                response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
            }

            if (!response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
                response.headers().set(HttpHeaders.CONTENT_LENGTH, response.content().readableBytes());
            }
            return response;
        }

        byte[] content = toContentBytes(o);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
        response.headers().setInt(HttpHeaders.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static byte[] toContentBytes(Object contentObject) {
        try {
            return FrameworkMisc.JACKSON_MAPPER.writeValueAsBytes(contentObject);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static FullHttpResponse buildSuccessResponse() {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
        response.headers().setInt(HttpHeaders.CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

}
