package org.imanity.framework.database;

import lombok.Getter;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.util.builder.SQLHost;
import org.imanity.framework.util.builder.hikari.HikariHandler;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.annotation.Comment;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;

@Service(name = "mysql")
@Getter
public class MySQL implements IService {

    private SQLConfig config;
    private SQLHost host;
    private DataSource dataSource;

    public void generateConfig() {
        this.config = new SQLConfig();
        config.loadAndSave();
    }

    @Override
    public void init() {
        this.generateConfig();
        if (true) {
//        if (!ImanityCommon.CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MYSQL)) {
            return;
        }

        HikariHandler.init();

        host = new SQLHost(config.HOST, config.USER, config.PORT, config.PASSWORD, config.DATABASE);
        this.dataSource = HikariHandler.createDataSource(host);
    }

    @Override
    public void stop() {
        HikariHandler.closeDataSource(this.host);
    }

    public static class SQLConfig extends YamlConfiguration {

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

        public SQLConfig() {
            super(new File(ImanityCommon.BRIDGE.getDataFolder(), "mysql.yml").toPath(), YamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                .setPrependedComments(Arrays.asList(
                        "================================",
                        " ",
                        "The configuration to config mysql stuffs",
                        " ",
                        "================================",
                        " "
                )).build());
        }

    }

}
