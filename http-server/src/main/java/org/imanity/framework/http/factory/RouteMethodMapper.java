package org.imanity.framework.http.factory;

import com.google.common.collect.Sets;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.imanity.framework.ComponentHolder;
import org.imanity.framework.ComponentRegistry;
import org.imanity.framework.http.RestController;
import org.imanity.framework.http.annotation.GetMapping;
import org.imanity.framework.http.annotation.PostMapping;
import org.imanity.framework.http.entity.MethodDetail;
import org.imanity.framework.util.entry.Entry;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

public class RouteMethodMapper {

    private static final Set<Entry<Pattern, MethodMapping>> GET_MAPPINGS = Sets.newConcurrentHashSet();
    private static final Set<Entry<Pattern, MethodMapping>> POST_MAPPINGS = Sets.newConcurrentHashSet();

    public static void preInit() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {RestController.class};
            }

            @Override
            public Object newInstance(Class<?> type) {

                RestController instance = (RestController) super.newInstance(type);
                String baseUrl = instance.baseUrl();

                for (Method method : type.getDeclaredMethods()) {
                    GetMapping getAnnotation = method.getAnnotation(GetMapping.class);
                    if (getAnnotation != null) {
                        String url = baseUrl + getAnnotation.value();
                        String formattedUrl = formatUrl(url);
                        MethodMapping mapping = new MethodMapping(instance, method, url);
                        GET_MAPPINGS.add(new Entry<>(Pattern.compile(formattedUrl), mapping));
                    }

                    PostMapping postAnnotation = method.getAnnotation(PostMapping.class);
                    if (postAnnotation != null) {
                        String url = baseUrl + postAnnotation.value();
                        String formattedUrl = formatUrl(url);
                        MethodMapping mapping = new MethodMapping(instance, method, url);
                        POST_MAPPINGS.add(new Entry<>(Pattern.compile(formattedUrl), mapping));
                    }
                }
                return instance;
            }
        });
    }

    public static MethodDetail getMethodDetail(String requestPath, HttpMethod httpMethod) {
        MethodDetail methodDetail = new MethodDetail();
        if (httpMethod == HttpMethod.GET) {
            methodDetail.build(requestPath, GET_MAPPINGS);
            return methodDetail;
        }

        if (httpMethod == HttpMethod.POST) {
            methodDetail.build(requestPath, POST_MAPPINGS);
            return methodDetail;
        }
        return null;
    }

    private static String formatUrl(String url) {
        String originPattern = url.replaceAll("(\\{\\w+})", "[\\\\u4e00-\\\\u9fa5_a-zA-Z0-9]+");
        String pattern = "^" + originPattern + "/?$";
        return pattern.replaceAll("/+", "/");
    }

    @AllArgsConstructor
    @Data
    public static class MethodMapping {

        private Object instance;
        private Method method;
        private String url;

    }

}
