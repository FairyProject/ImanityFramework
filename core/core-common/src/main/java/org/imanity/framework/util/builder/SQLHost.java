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

package org.imanity.framework.util.builder;

import org.imanity.framework.util.Strings;

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
