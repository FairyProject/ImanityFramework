package org.imanity.framework.http.resolver;

import org.imanity.framework.http.entity.MethodDetail;

import java.lang.reflect.Parameter;

public interface ParameterResolver {

    Object resolve(MethodDetail methodDetail, Parameter parameter);

}
