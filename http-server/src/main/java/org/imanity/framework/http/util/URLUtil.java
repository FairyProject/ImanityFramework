package org.imanity.framework.http.util;

import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.CharEncoding;

public class URLUtil {

    public static String getRequestPath(String uri) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charsets.toCharset(CharEncoding.UTF_8));
        return queryDecoder.path();
    }

}
