package org.imanity.framework.util;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;

/**
 * Represents an object that knows it's own type parameter.
 *
 * @param <T> the type
 */
public interface TypeAware<T> {

    @Nonnull
    TypeToken<T> getType();

}