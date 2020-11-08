package org.imanity.framework.http.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.io.Charsets;
import org.imanity.framework.http.entity.MethodDetail;
import org.imanity.framework.http.factory.FullHttpResponseFactory;
import org.imanity.framework.http.factory.ParameterResolverFactory;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.http.resolver.ParameterResolver;
import org.imanity.framework.http.util.URLUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequestHandler implements RequestHandler {
    @Override
    public FullHttpResponse handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        Map<String, String> queryParameterMappings = getQueryParams(requestUri);

        String requestPath = URLUtil.getRequestPath(requestUri);

        MethodDetail methodDetail = RouteMethodMapper.getMethodDetail(requestPath, HttpMethod.GET);
        if (methodDetail == null) {
            return null;
        }
        methodDetail.setQueryParameterMappings(queryParameterMappings);
        Method targetMethod = methodDetail.getMethod();
        if (targetMethod == null) {
            return null;
        }
        Parameter[] targetMethodParameters = targetMethod.getParameters();

        List<Object> targetMethodParams = new ArrayList<>();
        for (Parameter parameter : targetMethodParameters) {
            ParameterResolver parameterResolver = ParameterResolverFactory.get(parameter);
            if (parameterResolver != null) {
                Object param = parameterResolver.resolve(methodDetail, parameter);
                targetMethodParams.add(param);
            }
        }

        return FullHttpResponseFactory.getSuccessResponse(targetMethod, targetMethodParams, methodDetail.getInstance());
    }

    private Map<String, String> getQueryParams(String uri) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.UTF_8);
        Map<String, List<String>> parameters = queryDecoder.parameters();
        Map<String, String> queryParams = new HashMap<>();
        for (Map.Entry<String, List<String>> attr : parameters.entrySet()) {
            for (String attrVal : attr.getValue()) {
                queryParams.put(attr.getKey(), attrVal);
            }
        }
        return queryParams;
    }
}
