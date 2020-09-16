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
            "redisson",
            "3.13.3",
            "Wx16na9jIrQP0ezbW0W8ATDP0fBlID8GXnosT4QGU4w="
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
    );

    private final String mavenRepoPath;
    private final String version;
    private final byte[] checksum;

    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Library(String groupId, String artifactId, String version, String checksum) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                groupId.replace(".", "/"),
                artifactId,
                version,
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
