/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.redis;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.plugin.component.ComponentHolder;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.redis.message.Message;
import org.imanity.framework.redis.message.MessageListener;
import org.imanity.framework.redis.message.transformer.FieldTransformer;
import org.imanity.framework.redis.server.ServerHandler;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;

@Service(name = "redis")
@Getter
public class ImanityRedis implements IService {

    private RedissonClient client;
    private ServerHandler serverHandler;
    private RedisConfig redisConfig;

    public void generateConfig() {
        this.redisConfig = new RedisConfig();
        redisConfig.loadAndSave();
    }

    @Override
    public void preInit() {
        if (!ImanityCommon.CORE_CONFIG.USE_REDIS) {
            return;
        }

        this.serverHandler = new ServerHandler(this);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {

            @Override
            public Object newInstance(Class<?> type) {
                Object instance = super.newInstance(type);
                serverHandler.getMessageHandler().registerListener(instance);

                return instance;
            }

            @Override
            public Class<?>[] type() {
                return new Class[] {MessageListener.class};
            }

        });
    }

    @Override
    public void init() {
        this.generateConfig();
        if (!ImanityCommon.CORE_CONFIG.USE_REDIS) {
            return;
        }

        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://" + redisConfig.IP_ADDRESS + ":" + redisConfig.PASSWORD);
        if (redisConfig.hasPassword()) {
            config.useClusterServers()
                    .setPassword(redisConfig.PASSWORD);
        }

        this.client = Redisson.create(config);
        this.serverHandler.init();
    }

    @Override
    public void stop() {
        if (!ImanityCommon.CORE_CONFIG.USE_REDIS) {
            return;
        }
        this.serverHandler.shutdown();

        this.client.shutdown();
    }

    public RReadWriteLock getLock(String name) {
        return this.client.getReadWriteLock(name);
    }

    public RMap<String, String> getMap(String name) {
        return this.client.getMap(name);
    }

    public Iterable<String> getKeys(String pattern) {
        return this.client.getKeys().getKeysByPattern(pattern);
    }

    public void registerListener(MessageListener messageListener) {
        this.getServerHandler().getMessageHandler().registerListener(messageListener);
    }

    public void registerMessage(Class<? extends Message> messageClass) {
        this.getServerHandler().getMessageHandler().registerMessage(messageClass);
    }

    public void sendMessage(Message message) {
        this.getServerHandler().getMessageHandler().sendMessage(message);
    }

    public <T> void registerTransformer(Class<?> type, FieldTransformer<T> transformer) {
        this.getServerHandler().getMessageHandler().registerTransformer(type, transformer);
    }

    public <T> FieldTransformer<T> getTransformer(Class<?> type) {
        return this.getServerHandler().getMessageHandler().getTransformer(type);
    }

    public static class RedisConfig extends YamlConfiguration {

        public String IP_ADDRESS = "localhost";
        public int PORT = 6379;

        public String PASSWORD = "";

        protected RedisConfig() {
            super(new File(ImanityCommon.BRIDGE.getDataFolder(), "redis.yml").toPath(), YamlProperties
                    .builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }

        public boolean hasPassword() {
            return PASSWORD != null && PASSWORD.length() > 0;
        }
    }

}
