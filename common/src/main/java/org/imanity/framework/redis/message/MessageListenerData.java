package org.imanity.framework.redis.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class MessageListenerData {

    private final Object instance;
    private final Method method;
    private final Class<? extends Message> messageClass;

}
