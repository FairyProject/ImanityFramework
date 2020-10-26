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

package org.imanity.framework.database;

import lombok.Getter;
import org.imanity.framework.annotation.PostDestroy;
import org.imanity.framework.annotation.PostInitialize;
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
public class MySQL {

    private SQLConfig config;
    private SQLHost host;
    private DataSource dataSource;

    public void generateConfig() {
        this.config = new SQLConfig();
        config.loadAndSave();
    }

    @PostInitialize
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

    @PostDestroy
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
