package org.imanity.framework.http.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AsciiString;
import org.imanity.framework.Autowired;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.http.exception.ErrorResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class FullHttpResponseFactory {

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

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
        byte[] content;
        try {
            content = FrameworkMisc.JACKSON_MAPPER.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, httpResponseStatus, Unpooled.wrappedBuffer(content));
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }


    private static FullHttpResponse buildSuccessResponse(Object o) {
        byte[] content;
        try {
            content = FrameworkMisc.JACKSON_MAPPER.writeValueAsBytes(o);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(content));
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    private static FullHttpResponse buildSuccessResponse() {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

}
