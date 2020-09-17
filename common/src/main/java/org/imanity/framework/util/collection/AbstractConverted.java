package org.imanity.framework.util.collection;

import javax.annotation.Nullable;

import com.google.common.base.Function;

/**
 * Represents an object that transform elements of type VInner to type VOuter and back again.
 *
 * @credit ProtocolLib
 *
 * @param <VInner> - the first type.
 * @param <VOuter> - the second type.
 */
public abstract class AbstractConverted<VInner, VOuter> {
    // Inner conversion
    private Function<VOuter, VInner> innerConverter = new Function<VOuter, VInner>() {
        @Override
        public VInner apply(@Nullable VOuter param) {
            return toInner(param);
        }
    };

    // Outer conversion
    private Function<VInner, VOuter> outerConverter = new Function<VInner, VOuter>() {
        @Override
        public VOuter apply(@Nullable VInner param) {
            return toOuter(param);
        }
    };

    /**
     * Convert a value from the inner map to the outer visible map.
     * @param inner - the inner value.
     * @return The outer value.
     */
    protected abstract VOuter toOuter(VInner inner);

    /**
     * Convert a value from the outer map to the internal inner map.
     * @param outer - the outer value.
     * @return The inner value.
     */
    protected abstract VInner toInner(VOuter outer);

    /**
     * Retrieve a function delegate that converts outer objects to inner objects.
     * @return A function delegate.
     */
    protected Function<VOuter, VInner> getInnerConverter() {
        return innerConverter;
    }

    /**
     * Retrieve a function delegate that converts inner objects to outer objects.
     * @return A function delegate.
     */
    protected Function<VInner, VOuter> getOuterConverter() {
        return outerConverter;
    }
}