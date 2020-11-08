package org.imanity.framework.http.netty;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.http.factory.FullHttpResponseFactory;
import org.imanity.framework.http.factory.RequestHandlerFactory;
import org.imanity.framework.http.handler.RequestHandler;
import org.imanity.framework.http.util.URLUtil;

public class HttpServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String FAVICON_ICO = "/favicon.ico";
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

    private static final Logger LOGGER = LogManager.getLogger(HttpServiceHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        String uri = fullHttpRequest.uri();
        if (uri.equals(FAVICON_ICO)) {
            return;
        }
        RequestHandler requestHandler = RequestHandlerFactory.get(fullHttpRequest.method());
        FullHttpResponse fullHttpResponse;
        try {
            fullHttpResponse = requestHandler.handle(fullHttpRequest);
        } catch (Throwable e) {
            LOGGER.error("Caught an unexpected error.", e);
            String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
            fullHttpResponse = FullHttpResponseFactory.getErrorResponse(requestPath, e.toString(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        if (fullHttpResponse == null) {
            String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
            ctx.write(FullHttpResponseFactory.getErrorResponse(requestPath, "Nothing here", HttpResponseStatus.NO_CONTENT));
            return;
        }

        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        if (!keepAlive) {
            ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
        } else {
            fullHttpResponse.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(fullHttpResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


}