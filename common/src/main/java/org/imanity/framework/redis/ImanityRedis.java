package org.imanity.framework.redis;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
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

        this.serverHandler = new ServerHandler(this);
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
