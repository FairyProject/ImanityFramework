package org.imanity.framework.http.resolver;

import org.imanity.framework.http.annotation.PathVariable;
import org.imanity.framework.http.entity.MethodDetail;
import org.imanity.framework.http.util.ObjectUtil;

import java.lang.reflect.Parameter;
import java.util.Map;

public class ParameterResolverPathVariable implements ParameterResolver {

    @Override
    public Object resolve(MethodDetail methodDetail, Parameter parameter) {
        PathVariable pathVariable = parameter.getDeclaredAnnotation(PathVariable.class);
        String requestParameter = pathVariable.value();
        Map<String, String> urlParameterMappings = methodDetail.getUrlParameterMappings();
        String requestParameterValue = urlParameterMappings.get(requestParameter);
        return ObjectUtil.convert(parameter.getType(), requestParameterValue);
    }

}
