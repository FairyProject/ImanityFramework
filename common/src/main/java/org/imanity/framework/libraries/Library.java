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

package org.imanity.framework.libraries;

import lombok.Getter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@Getter
public enum Library {

    REDISSON(
            "org.redisson",
            "redisson-all", // Include all
            "3.13.3",
            "Wx16na9jIrQP0ezbW0W8ATDP0fBlID8GXnosT4QGU4w="
    ),

    /**
     *
     * Only in Imanity-Libraries repository
     * This is the netty relocated version of redisson
     *
     */
    REDISSON_RELOCATED(
            "org.redisson",
            "redisson-relocated",
            "3.13.7-SNAPSHOT",
            "3.13.7-20201024.150336-1",
            null
    ),

    YAML(
            "org.yaml",
            "snakeyaml",
            "1.20",
            "HOWEuJiOSesajRuXIHGgsg9eyONxV7xakRNP3ZDGEjw="
    ),
    HIKARI_CP(
            "com.zaxxer",
            "HikariCP",
            "3.1.0",
            "TBo58lIW2Ukyh3VYKUwOliccAeRx+y9FxdDzsD8UUUw="
    ),
    MONGO_DB(
            "org.mongodb",
            "mongo-java-driver",
            "3.12.2",
            "eMxHcEtasb/ubFCv99kE5rVZMPGmBei674ZTdjYe58w="
    ),
    CAFFEINE(
            "com.github.ben-manes.caffeine",
            "caffeine",
            "2.8.4",
            "KV9YN5gQj6b507VJApJpPF5PkCon0DZqAi0T7Ln0lag="
    ),
    GUAVA(
            "com.google.guava",
            "guava",
            "29.0-jre",
            "SIXFTM1H57LSJTHQSY+RW1FY6AQGTA7NKCYL+WEW2IU="
    ),
    REFLECTIVE_ASM(
            "com.esotericsoftware",
            "reflectasm",
            "1.11.9",
            null
    ),
    JACKSON_DATABIND(
            "com.fasterxml.jackson.core",
            "jackson-databind",
            "2.11.2",
            "y4kLSq2O0hp7V+PI95JNvcoa7/nd0nyw/zckMDeuE0I="
    ),
    FAST_UTIL(
            "it.unimi.dsi",
            "fastutil",
            "8.1.0",
            null
    );

    private final String mavenRepoPath;
    private final String version;
    private final byte[] checksum;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Library(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, version, checksum);
    }

    Library(String groupId, String artifactId, String versionPackage, String version, String checksum) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                groupId.replace(".", "/"),
                artifactId,
                versionPackage,
                artifactId,
                version
        );
        this.version = version;
        if (checksum != null) {
            this.checksum = Base64.getDecoder().decode(checksum);
        } else {
            this.checksum = null;
        }
    }

    public String getFileName() {
        return name().toLowerCase().replace('_', '-') + "-" + this.version;
    }

    public boolean checksumMatches(byte[] hash) {
        if (this.checksum == null) {
            return true;
        }
        return Arrays.equals(this.checksum, hash);
    }

    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
