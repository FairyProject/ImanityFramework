package org.imanity.framework.http.resolver;

import org.imanity.framework.http.annotation.RequestParam;
import org.imanity.framework.http.entity.MethodDetail;
import org.imanity.framework.http.util.ObjectUtil;

import java.lang.reflect.Parameter;

public class ParameterResolverRequestParam implements ParameterResolver {

    @Override
    public Object resolve(MethodDetail methodDetail, Parameter parameter) {
        RequestParam requestParam = parameter.getDeclaredAnnotation(RequestParam.class);
        String requestParameter = requestParam.value();
        String requestParameterValue = methodDetail.getQueryParameterMappings().get(requestParameter);
        if (requestParameterValue == null) {
            throw new IllegalArgumentException("The specified parameter " + requestParameter + " can not be null!");
        }
        // convert the parameter to the specified type
        return ObjectUtil.convert(parameter.getType(), requestParameterValue);
    }
}
