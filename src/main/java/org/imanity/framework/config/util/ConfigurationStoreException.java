package org.imanity.framework.config.util;

/**
 * Signals that an error occurred while storing or loading a configuration.
 */
public final class ConfigurationStoreException extends RuntimeException {
    public ConfigurationStoreException(Throwable cause) {
        super(cause);
    }
}
