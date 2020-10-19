package org.imanity.framework.boot.user;

import java.util.UUID;

public interface UserInterface<T> {

    Class<T> type();

    String name(T user);

    UUID uuid(T user);

}
