package org.imanity.framework.command.parameter;

import org.imanity.framework.command.CommandEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface ParameterHolder<T> {

    Class[] type();

    T transform(CommandEvent commandEvent, String source);

    default List<String> tabComplete(Object user, Set<String> flags, String source) {
        return Collections.emptyList();
    }

    default List<String> tabComplete(Object user, String[] parameters, Set<String> flags, String source) {
        return this.tabComplete(user, flags, source);
    }

}
