package org.imanity.framework.http.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.http.annotation.RequestBody;
import org.imanity.framework.http.entity.MethodDetail;

import java.lang.reflect.Parameter;

public class ParameterResolverRequestBody implements ParameterResolver {

    @Override
    public Object resolve(MethodDetail methodDetail, Parameter parameter) {
        Object param = null;
        RequestBody requestBody = parameter.getDeclaredAnnotation(RequestBody.class);
        if (requestBody != null) {
            try {
                param = FrameworkMisc.JACKSON_MAPPER.readValue(methodDetail.getJson(), parameter.getType());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return param;
    }
}
