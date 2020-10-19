package org.imanity.framework.boot.error;

public interface ErrorHandler {

    Class<? extends Throwable>[] types();

    boolean handle(Throwable throwable);

}
