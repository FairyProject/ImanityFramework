package org.imanity.framework.database;

import lombok.Getter;
import me.skymc.taboolib.mysql.builder.SQLHost;
import me.skymc.taboolib.mysql.builder.hikari.HikariHandler;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.config.util.annotation.Comment;
import org.imanity.framework.config.util.yaml.BukkitYamlConfiguration;
import org.imanity.framework.config.util.format.FieldNameFormatters;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;

@Getter
public class MySQL {

    private SQLConfig config;
    private SQLHost host;
    private DataSource dataSource;

    public MySQL() {
    }

    public void init(Plugin plugin) {
        this.config = new SQLConfig(plugin);
        config.loadAndSave();

        HikariHandler.init();

        host = new SQLHost(config.HOST, config.USER, config.PORT, config.PASSWORD, config.DATABASE);
        this.dataSource = HikariHandler.createDataSource(host);
    }

    public static class SQLConfig extends BukkitYamlConfiguration {

        @Comment("host ip address")
        public String HOST = "localhost";
        @Comment({"host ip port", "must be greater than 1024"})
        public String PORT = "3306";
        @Comment("mysql user name")
        public String USER = "user";
        @Comment("mysql user password")
        public String PASSWORD = "password";
        @Comment({"mysql database", "all data will store inside this database"})
        public String DATABASE = "database";
        @Comment("The prefix of tables")
        public String TABLE_PREFIX = "imanity-";

        public SQLConfig(Plugin plugin) {
            super(new File(plugin.getDataFolder(), "mysql.yml").toPath(), BukkitYamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_UNDERSCORE)
                .setPrependedComments(Arrays.asList(
                        "================================",
                        " ",
                        "The configuration to config mysql stuffs",
                        " ",
                        "================================"
                )).build());
        }

    }

}
