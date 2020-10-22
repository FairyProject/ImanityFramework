package org.imanity.framework.command;

public interface CommandProvider {

    boolean hasPermission(Object user, String permission);

}
