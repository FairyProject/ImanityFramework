package org.imanity.framework.libraries;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public enum LibraryRepository {

    MAVEN_CENTRAL("https://repo1.maven.org/maven2/");

    private final String url;

    LibraryRepository(String url) {
        this.url = url;
    }

    protected URLConnection openConnection(Library library) throws IOException {
        URL url = new URL(this.url + library.getMavenRepoPath());
        return url.openConnection();
    }

    public byte[] downloadRaw(Library dependency) throws LibraryDownloadException {
        try {
            URLConnection connection = openConnection(dependency);
            try (InputStream in = connection.getInputStream()) {
                byte[] bytes = ByteStreams.toByteArray(in);
                if (bytes.length == 0) {
                    throw new LibraryDownloadException("Empty stream");
                }
                return bytes;
            }
        } catch (Exception e) {
            throw new LibraryDownloadException(e);
        }
    }

    public byte[] download(Library dependency) throws LibraryDownloadException {
        byte[] bytes = downloadRaw(dependency);

        // compute a hash for the downloaded file
        byte[] hash = Library.createDigest().digest(bytes);

        // ensure the hash matches the expected checksum
        if (!dependency.checksumMatches(hash)) {
            throw new LibraryDownloadException("Downloaded file had an invalid hash. " +
                    "Expected: " + Base64.getEncoder().encodeToString(dependency.getChecksum()) + " " +
                    "Actual: " + Base64.getEncoder().encodeToString(hash));
        }

        return bytes;
    }

    public void download(Library dependency, Path file) throws LibraryDownloadException {
        try {
            Files.write(file, download(dependency));
        } catch (IOException e) {
            throw new LibraryDownloadException(e);
        }
    }
}
