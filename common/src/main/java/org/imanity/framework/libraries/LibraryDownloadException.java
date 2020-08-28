package org.imanity.framework.libraries;

public class LibraryDownloadException extends Exception {

    public LibraryDownloadException() {

    }

    public LibraryDownloadException(String message) {
        super(message);
    }

    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibraryDownloadException(Throwable cause) {
        super(cause);
    }
}