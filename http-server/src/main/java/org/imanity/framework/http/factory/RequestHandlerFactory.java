package org.imanity.framework.http.factory;

import io.netty.handler.codec.http.HttpMethod;
import org.imanity.framework.http.handler.GetRequestHandler;
import org.imanity.framework.http.handler.PostRequestHandler;
import org.imanity.framework.http.handler.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class RequestHandlerFactory {
    public static final Map<HttpMethod, RequestHandler> REQUEST_HANDLERS = new HashMap<>();

    static {
        REQUEST_HANDLERS.put(HttpMethod.GET, new GetRequestHandler());
        REQUEST_HANDLERS.put(HttpMethod.POST, new PostRequestHandler());
    }

    public static RequestHandler get(HttpMethod httpMethod) {
        return REQUEST_HANDLERS.get(httpMethod);
    }
}
