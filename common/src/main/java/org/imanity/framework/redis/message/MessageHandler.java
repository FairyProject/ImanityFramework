package org.imanity.framework.redis.message;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.message.annotation.AutoWiredMessageListener;
import org.imanity.framework.redis.message.annotation.HandleMessage;
import org.imanity.framework.redis.subscription.RedisPubSub;
import org.imanity.framework.util.CommonUtility;
import org.imanity.framework.util.annotation.AnnotationDetector;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageHandler {

    private RedisPubSub redisPubSub;
    private String channel;

    private final Int2ObjectMap<List<MessageListenerData>> messageListeners = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Class<? extends Message>> idToClass = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<Class<? extends Message>> classToId = new Object2IntOpenHashMap<>();


    public MessageHandler(String channel) {
        this.channel = channel;

        this.redisPubSub = new RedisPubSub(this.channel, ImanityCommon.REDIS);
        this.setup();
    }

    public void sendMessage(Message message) {
        try {
            JsonObject jsonObject = message.serialize();
            if (jsonObject == null) {
                throw new IllegalStateException("The Message given a null serialized data!");
            }

            this.redisPubSub.publish(message.id(), jsonObject);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Message buildMessage(int id) {
        Class<? extends Message> messageClass = this.idToClass.get(id);
        if (messageClass == null) {
            throw new IllegalStateException("The message id " + id + " does not exists!");
        }
        try {
            return messageClass.newInstance();
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not create new instance of message", throwable);
        }
    }

    public void registerMessage(Class<? extends Message> messageClass) {
        int id = Objects.hash(messageClass.getPackage().getName(), messageClass.getSimpleName());
        this.registerMessage(id, messageClass);
    }

    public void registerMessage(int id, Class<? extends Message> messageClass) {
        if (this.idToClass.containsKey(id)) {
            throw new IllegalStateException("The Message ID " + id + " already exists!");
        }

        this.messageListeners.put(id, new ArrayList<>());
        this.idToClass.put(id, messageClass);
        this.classToId.put(messageClass, id);
    }

    public void registerListenersByAnnotation() {

        CommonUtility.tryCatch(() -> {
            new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {

                    CommonUtility.tryCatch(() -> {

                        Class<?> type = Class.forName(className);
                        Object listener = type.newInstance();

                        registerListener(listener);

                    });

                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] { AutoWiredMessageListener.class };
                }
            }).detect(ImanityCommon.BRIDGE.getPluginFiles().toArray(new File[0]));
        });

    }

    public void registerListener(Object messageListener) {
        Method[] methods = messageListener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.getDeclaredAnnotation(HandleMessage.class) == null) {
                continue;
            }
            if (method.getParameters().length != 1 || !Message.class.isAssignableFrom(method.getParameters()[0].getType())) {
                continue;
            }
            Class<? extends Message> messageClass = method.getParameters()[0].getClass().asSubclass(Message.class);

            int id = this.classToId.getInt(messageClass);
            List<MessageListenerData> listeners = this.messageListeners.get(id);
            if (listeners == null) {
                listeners = new ArrayList<>();
                this.messageListeners.put(id, listeners);
            }
            listeners.add(new MessageListenerData(messageListener, method, messageClass));
        }
    }

    public void setup() {
        this.redisPubSub.subscribe(((payload, jsonObject) -> {
            int id = Integer.parseInt(payload);

            Message message = this.buildMessage(id);
            if (message == null) {
                return;
            }

            message.deserialize(jsonObject);
            List<MessageListenerData> listeners = this.messageListeners.get(id);
            if (listeners == null) {
                return;
            }
            for (MessageListenerData data : listeners) {
                try {
                    data.getMethod().invoke(data.getInstance(), message);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }));
    }

}
