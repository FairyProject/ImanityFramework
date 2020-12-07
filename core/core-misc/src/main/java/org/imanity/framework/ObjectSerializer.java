package org.imanity.framework;

public interface ObjectSerializer<I, O> {

    O serialize(I input);
    I deserialize(O output);

    Class<I> inputClass();
    Class<O> outputClass();

}
