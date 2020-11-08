package org.imanity.framework.http.factory;

import org.imanity.framework.http.annotation.PathVariable;
import org.imanity.framework.http.annotation.RequestBody;
import org.imanity.framework.http.annotation.RequestParam;
import org.imanity.framework.http.resolver.ParameterResolver;
import org.imanity.framework.http.resolver.ParameterResolverPathVariable;
import org.imanity.framework.http.resolver.ParameterResolverRequestBody;
import org.imanity.framework.http.resolver.ParameterResolverRequestParam;

import java.lang.reflect.Parameter;

public class ParameterResolverFactory {

    public static ParameterResolver get(Parameter parameter) {
        if (parameter.isAnnotationPresent(RequestParam.class)) {
            return new ParameterResolverRequestParam();
        }
        if (parameter.isAnnotationPresent(PathVariable.class)) {
            return new ParameterResolverPathVariable();
        }
        if (parameter.isAnnotationPresent(RequestBody.class)) {
            return new ParameterResolverRequestBody();
        }
        return null;
    }
}
