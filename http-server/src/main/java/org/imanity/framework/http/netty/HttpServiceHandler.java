/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import org.imanity.framework.http.exception.BadRequestException;
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

        FullHttpResponse fullHttpResponse;
        RequestHandler requestHandler = RequestHandlerFactory.get(fullHttpRequest.method());
        if (requestHandler == null) {
            String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
            fullHttpResponse = FullHttpResponseFactory.getErrorResponse(requestPath, "Unexpected request method: " + fullHttpRequest.method(), HttpResponseStatus.METHOD_NOT_ALLOWED);
        } else {
            try {
                fullHttpResponse = requestHandler.handle(fullHttpRequest);
            } catch (BadRequestException e) {

                LOGGER.warn("Bad Request: " + e.getLocalizedMessage());
                String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
                fullHttpResponse = FullHttpResponseFactory.getErrorResponse(requestPath, e.toString(), HttpResponseStatus.BAD_REQUEST);

            } catch (Throwable e) {
                LOGGER.error("Caught an unexpected error.", e);
                String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
                fullHttpResponse = FullHttpResponseFactory.getErrorResponse(requestPath, e.toString(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (fullHttpResponse == null) {
            String requestPath = URLUtil.getRequestPath(fullHttpRequest.uri());
            fullHttpResponse = FullHttpResponseFactory.getErrorResponse(requestPath, "Nothing here", HttpResponseStatus.NO_CONTENT);
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