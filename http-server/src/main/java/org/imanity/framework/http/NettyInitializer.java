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

package org.imanity.framework.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import org.imanity.framework.http.cors.AbstractCorsConfiguration;
import org.imanity.framework.http.netty.HttpServiceHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NettyInitializer {

    private final HttpService httpService;

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Thread thread;

    public NettyInitializer(HttpService httpService) {
        this.httpService = httpService;
    }

    public void init() {
        List<CorsConfig> corsConfigs = new ArrayList<>();
        boolean shortCircuit = false;
        for (AbstractCorsConfiguration corsConfiguration : this.httpService.getCorsConfigurations()) {
            CorsConfigBuilder builder;

            String[] origins = corsConfiguration.origin();
            if (origins.length < 1) {
                continue;
            } else if (origins.length == 1) {
                builder = CorsConfigBuilder.forOrigin(origins[0]);
            } else {
                builder = CorsConfigBuilder.forOrigins(origins);
            }

            HttpMethod[] methods = corsConfiguration.allowMethods();
            if (methods != null && methods.length > 0) {
                builder.allowedRequestMethods(methods);
            }

            String[] headers = corsConfiguration.allowHeaders();
            if (headers != null && headers.length > 0) {
                builder.allowedRequestHeaders(headers);
            }

            CorsConfigBuilder result = corsConfiguration.setupConfig(builder);
            CorsConfig config = result.build();

            if (config.isShortCircuit()) {
                shortCircuit = true;
            }

            corsConfigs.add(config);
        }

        this.httpService.getCorsConfigurations().clear();

        boolean finalShortCircuit = shortCircuit;
        this.thread = new Thread(() -> {
            this.bossGroup = new NioEventLoopGroup(1);
            this.workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.TCP_NODELAY, this.httpService.getBootable().getBoolean("http.tcpNoDelay", true))
                        .childOption(ChannelOption.SO_KEEPALIVE, this.httpService.getBootable().getBoolean("http.keepAlive", true))
                        .option(ChannelOption.SO_BACKLOG, this.httpService.getBootable().getInteger("http.backlog", 128))
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                pipeline.addLast("decoder", new HttpRequestDecoder())
                                        .addLast("encoder", new HttpResponseEncoder())
                                        .addLast("aggregator", new HttpObjectAggregator(512 * 1024));

                                if (!corsConfigs.isEmpty()) {
                                    pipeline.addLast(new CorsHandler(corsConfigs, finalShortCircuit));
                                }

                                pipeline.addLast("handler", new HttpServiceHandler());
                            }
                        });
                int port = this.httpService.getBootable().getInteger("http.port", HttpService.DEFAULT_PORT);

                this.channel = b.bind(port).sync().channel();
                this.httpService.setRunning(true);
                HttpService.LOGGER.info("Start HTTP server with port {}", port);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        this.thread.setName("HTTP Server Thread");
        this.thread.setDaemon(true);

        this.thread.start();
    }

    public void stop() throws Exception {
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();

        this.channel.closeFuture().sync();

        this.thread.join();
        this.thread.interrupt();
    }

}
