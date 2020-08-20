package org.imanity.framework.redis.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class MessageListenerData {

    private final MessageListener instance;
    private final Method method;
    private final Class<? extends Message> messageClass;

}
