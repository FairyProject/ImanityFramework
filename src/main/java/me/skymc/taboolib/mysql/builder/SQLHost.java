package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;

import java.util.Objects;


public class SQLHost {

    private String host;
    private String user;
    private String port;
    private String password;
    private String database;

    public SQLHost(String host, String user, String port, String password, String database) {
        this.host = host;
        this.user = user;
        this.port = port;
        this.password = password;
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getConnectionUrl() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}?characterEncoding=utf-8&useSSL=false", this.host, this.port, this.database);
    }

    public String getConnectionUrlSimple() {
        return Strings.replaceWithOrder("jdbc:mysql://{0}:{1}/{2}", this.host, this.port, this.database);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SQLHost)) {
            return false;
        }
        SQLHost sqlHost = (SQLHost) o;
        return Objects.equals(getHost(), sqlHost.getHost()) &&
                Objects.equals(getUser(), sqlHost.getUser()) &&
                Objects.equals(getPort(), sqlHost.getPort()) &&
                Objects.equals(getPassword(), sqlHost.getPassword()) &&
                Objects.equals(getDatabase(), sqlHost.getDatabase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getUser(), getPort(), getPassword(), getDatabase());
    }

    @Override
    public String toString() {
        return "SQLHost{" +
                "host='" + host + '\'' +
                ", user='" + user + '\'' +
                ", port='" + port + '\'' +
                ", password='" + password + '\'' +
                ", database='" + database + '\'' +
                '}';
    }
}
