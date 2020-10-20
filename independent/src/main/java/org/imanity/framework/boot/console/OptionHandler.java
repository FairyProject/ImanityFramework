package org.imanity.framework.boot.console;

public interface OptionHandler<T> {

    String[] option();

    default String description() {
        return "";
    }

    default void call(T result) {

    }

}
