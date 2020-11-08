package org.imanity.framework.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.*;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.http.netty.HttpServiceHandler;
import org.imanity.framework.libraries.Library;

/**
 *
 * Http Server Service
 *
 * Original by Snailclimb
 * Link: https://github.com/Snailclimb/jsoncat
 *
 */
@Service(name = "http")
public class HttpService {

    public static final int DEFAULT_PORT = 8080;
    private static final Logger LOGGER = LogManager.getLogger(HttpService.class);

    @Autowired
    private FrameworkBootable bootable;
    private Thread thread;

    @PreInitialize
    public void preInit() {
        try {
            Class.forName("io.netty.handler.codec.http.HttpRequestDecoder");
        } catch (ClassNotFoundException ex) {
            Library library = new Library(
                    "io.netty",
                    "netty-codec-http",
                    "4.1.42.Final",
                    null
            );
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(library);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(library);
        }

        RouteMethodMapper.preInit();
    }

    @PostInitialize
    public void init() {
        this.thread = new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.TCP_NODELAY, this.bootable.getBoolean("http.tcpNoDelay", true))
                        .childOption(ChannelOption.SO_KEEPALIVE, this.bootable.getBoolean("http.keepAlive", true))
                        .option(ChannelOption.SO_BACKLOG, this.bootable.getInteger("http.backlog", 128))
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast("decoder", new HttpRequestDecoder())
                                        .addLast("encoder", new HttpResponseEncoder())
                                        .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                                        .addLast("handler", new HttpServiceHandler());
                            }
                        });
                int port = this.bootable.getInteger("http.port", DEFAULT_PORT);

                Channel ch = b.bind(port).sync().channel();
                LOGGER.info("Start HTTP server with port {}", port);
                ch.closeFuture().sync();
            } catch (InterruptedException e) {
                LOGGER.error("Something wrong while handling http server", e);
            } finally {
                LOGGER.error("shutdown bossGroup and workerGroup");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });

        this.thread.setName("HTTP Server Thread");
        this.thread.setDaemon(true);

        this.thread.start();
    }

    @PreDestroy
    @SneakyThrows
    public void destroy() {
        this.thread.join();
    }

}
