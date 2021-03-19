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

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.*;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.http.cors.AbstractCorsConfiguration;
import org.imanity.framework.http.factory.RouteMethodMapper;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.relocate.Relocate;

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
@Getter
public class HttpService {

    public static final int DEFAULT_PORT = 8080;
    public static final Logger LOGGER = LogManager.getLogger(HttpService.class);

    @Autowired
    private FrameworkBootable bootable;
    @Setter
    private boolean running;
    private NettyInitializer nettyInitializer;

    private List<AbstractCorsConfiguration> corsConfigurations;

    @PreInitialize
    public void preInit() {
        Library nettyLibrary = new Library(
                "io{}netty",
                "netty-all",
                "4.1.59.Final",
                null,
                new Relocate("io{}netty", Library.IMANITY_LIB_PACKAGE + "netty")
        );

        Library nettyCodecLibrary = new Library(
                "io{}netty",
                "netty-codec-http",
                "4.1.59.Final",
                null,
                new Relocate("io{}netty", Library.IMANITY_LIB_PACKAGE + "netty")
        );

        ImanityCommon.LIBRARY_HANDLER.downloadLibraries(true, nettyLibrary, nettyCodecLibrary);

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
        this.nettyInitializer = new NettyInitializer(this);
        this.nettyInitializer.init();
    }

    @PreDestroy
    @SneakyThrows
    public void destroy() {
        if (!this.running) {
            return;
        }
        LOGGER.info("shutdown bossGroup and workerGroup");
        this.nettyInitializer.stop();
    }

}
