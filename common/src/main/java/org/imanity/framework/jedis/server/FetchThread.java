package org.imanity.framework.jedis.server;

import io.github.thatkawaiisam.jedis.helper.IRedisCommand;
import io.github.thatkawaiisam.jedis.helper.JedisHelper;
import org.imanity.framework.ImanityCommon;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class FetchThread extends Thread {

    private JedisHelper jedisHelper;

    public FetchThread(JedisHelper jedisHelper) {
        super();

        this.jedisHelper = jedisHelper;

        this.setName("Imanity Fetch Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        String prefix = ImanityCommon.METADATA_PREFIX + "Server";
        this.jedisHelper.runCommand(jedis -> {
            for (String key : jedis.keys(prefix + ":*")) {
                String name = key.substring(0, prefix.length());

                Map<String, String> data = jedis.hgetAll(key);
            }

            return jedis;
        });
    }
}
