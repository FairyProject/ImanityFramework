package org.imanity.framework.http.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;
import org.imanity.framework.http.entity.MethodDetail;
import org.imanity.framework.http.exception.BadRequestException;
import org.imanity.framework.http.factory.FullHttpResponseFactory;
import org.imanity.framework.http.factory.ParameterResolverFactory;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.http.resolver.ParameterResolver;
import org.imanity.framework.http.util.URLUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuang.kou
 * @createTime 2020年09月24日 13:33:00
 **/
public class PostRequestHandler implements RequestHandler {

    @Override
    public FullHttpResponse handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        // get http request path，such as "/user"
        String requestPath = URLUtil.getRequestPath(requestUri);
        // get target method
        MethodDetail methodDetail = RouteMethodMapper.getMethodDetail(requestPath, HttpMethod.POST);
        if (methodDetail == null) {
            return null;
        }
        Method targetMethod = methodDetail.getMethod();
        String contentType = this.getContentType(fullHttpRequest.headers());

        List<Object> targetMethodParams = new ArrayList<>();
        if (contentType != null && contentType.equals("application/json")) {
            String json = fullHttpRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
            methodDetail.setJson(json);
            Parameter[] targetMethodParameters = targetMethod.getParameters();
            for (Parameter parameter : targetMethodParameters) {
                ParameterResolver parameterResolver = ParameterResolverFactory.get(parameter);
                if (parameterResolver != null) {
                    Object param = parameterResolver.resolve(methodDetail, parameter);
                    targetMethodParams.add(param);
                }
            }

        } else {
            throw new BadRequestException("only receive application/json type data");
        }
        return FullHttpResponseFactory.getSuccessResponse(targetMethod, targetMethodParams, methodDetail.getInstance());
    }

    private String getContentType(HttpHeaders headers) {
        String contentType = headers.get("Content-Type");
        if (contentType == null) {
            return null;
        }
        String[] list = contentType.split(";");
        return list[0];
    }
}


