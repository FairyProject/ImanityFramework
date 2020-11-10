package org.imanity.framework.http.cors;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;

public interface AbstractCorsConfiguration {

    String[] origin();

    default HttpMethod[] allowMethods() {
        return null;
    }

    default String[] allowHeaders() {
        return null;
    }

    default CorsConfigBuilder setupConfig(CorsConfigBuilder builder) {
        return builder;
    }

}
