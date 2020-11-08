package org.imanity.framework.http.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.util.entry.Entry;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodDetail {

    private Method method;
    private Map<String, String> urlParameterMappings;
    private Map<String, String> queryParameterMappings;
    private String json;
    private Object instance;

    public void build(String requestPath, Set<Entry<Pattern, RouteMethodMapper.MethodMapping>> methodMappings) {
        for (Entry<Pattern, RouteMethodMapper.MethodMapping> entry : methodMappings) {
            Pattern pattern = entry.getKey();
            RouteMethodMapper.MethodMapping value = entry.getValue();

            boolean found = pattern.matcher(requestPath).find();
            if (found) {
                this.setInstance(value.getInstance());
                this.setMethod(value.getMethod());
                String url = value.getUrl();
                Map<String, String> urlParameterMappings = getUrlParameterMappings(requestPath, url);
                this.setUrlParameterMappings(urlParameterMappings);
            }
        }
    }

    private Map<String, String> getUrlParameterMappings(String requestPath, String url) {
        String[] requestParams = requestPath.split("/");
        String[] urlParams = url.split("/");
        Map<String, String> urlParameterMappings = new HashMap<>();
        for (int i = 1; i < urlParams.length; i++) {
            urlParameterMappings.put(urlParams[i].replace("{", "").replace("}", ""), requestParams[i]);
        }
        return urlParameterMappings;
    }

}
