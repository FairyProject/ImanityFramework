package org.imanity.framework.jedis;

import io.github.thatkawaiisam.jedis.helper.JedisCredentials;
import io.github.thatkawaiisam.jedis.helper.JedisHelper;
import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.jedis.server.ServerHandler;

import java.io.File;

@Getter
public class JedisHandler {

    private JedisHelper jedisHelper;
    private ServerHandler serverHandler;

    public void init() {
        JedisConfig config = new JedisConfig();
        config.loadAndSave();

        this.jedisHelper = new JedisHelper(new JedisCredentials(
                config.IP_ADDRESS,
                config.PASSWORD,
                config.PORT
        ));

        this.serverHandler = new ServerHandler(this);
        this.serverHandler.init();
    }

    private class JedisConfig extends YamlConfiguration {

        private String IP_ADDRESS;
        private int PORT;

        private String PASSWORD;

        protected JedisConfig() {
            super(new File(ImanityCommon.BRIDGE.getDataFolder(), "redis.yml").toPath(), YamlProperties
                    .builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }
    }
}
