package org.imanity.framework.redis;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.redis.server.ServerHandler;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;

@Getter
public class ImanityRedis {

    private RedissonClient client;
    private ServerHandler serverHandler;

    public void init() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.loadAndSave();

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
