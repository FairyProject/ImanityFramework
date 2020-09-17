package org.imanity.framework.util.collection;

import java.util.Collection;
import java.util.Set;

/**
 * Represents a set that wraps another set by transforming the items going in and out.
 *
 * @author Kristian
 *
 * @param <VInner> - type of the element in the inner invisible set.
 * @param <VOuter> - type of the elements publically accessible in the outer set.
 */
public abstract class ConvertedSet<VInner, VOuter> extends ConvertedCollection<VInner, VOuter> implements Set<VOuter> {
    public ConvertedSet(Collection<VInner> inner) {
        super(inner);
    }
}