package org.imanity.framework.boot.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserInterface<T> {

    Class<T> type();

    Collection<T> getAllUsers();

    String name(T user);

    UUID uuid(T user);

}
