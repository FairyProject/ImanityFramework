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
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.*;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.http.cors.AbstractCorsConfiguration;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.http.netty.HttpServiceHandler;
import org.imanity.framework.libraries.Library;

import java.util.ArrayList;
import java.util.List;

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

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private List<AbstractCorsConfiguration> corsConfigurations;

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

        this.corsConfigurations = new ArrayList<>();
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { AbstractCorsConfiguration.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                AbstractCorsConfiguration corsConfiguration = (AbstractCorsConfiguration) super.newInstance(type);

                corsConfigurations.add(corsConfiguration);
                return corsConfiguration;
            }
        });
    }

    @PostInitialize
    public void init() {
        List<CorsConfig> corsConfigs = new ArrayList<>();
        boolean shortCircuit = false;
        for (AbstractCorsConfiguration corsConfiguration : this.corsConfigurations) {
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

        this.corsConfigurations.clear();
        this.corsConfigurations = null;

        boolean finalShortCircuit = shortCircuit;
        this.thread = new Thread(() -> {
            this.bossGroup = new NioEventLoopGroup(1);
            this.workerGroup = new NioEventLoopGroup();

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
                int port = this.bootable.getInteger("http.port", DEFAULT_PORT);

                this.channel = b.bind(port).sync().channel();
                LOGGER.info("Start HTTP server with port {}", port);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        this.thread.setName("HTTP Server Thread");
        this.thread.setDaemon(true);

        this.thread.start();
    }

    @PreDestroy
    @SneakyThrows
    public void destroy() {
        LOGGER.error("shutdown bossGroup and workerGroup");
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();

        this.channel.closeFuture().sync();

        this.thread.join();
        this.thread.interrupt();
    }

}
