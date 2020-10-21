package org.imanity.framework.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InternalCommandEvent {

    private final Object user;
    private final String command;

}
