package org.imanity.framework.discord.input;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.units.qual.K;
import org.imanity.framework.PostInitialize;
import org.imanity.framework.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Service(name = "input")
public class InputService {

    private Map<Key, BiFunction<User, String, Boolean>> listeningConsumers;

    @PostInitialize
    public void init() {
        this.listeningConsumers = new ConcurrentHashMap<>(0);
    }

    public void handle(MessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot()) {
            return;
        }

        Key key = new Key(event.getChannel().getIdLong(), user.getIdLong());
        synchronized (this.listeningConsumers) {
            BiFunction<User, String, Boolean> consumer = this.listeningConsumers.getOrDefault(key, null);
            if (consumer != null) {
                boolean delete = consumer.apply(user, event.getMessage().getContentRaw());
                if (delete) {
                    this.listeningConsumers.remove(key);
                }
            }
        }
    }

    public void listen(long channel, User user, BiFunction<User, String, Boolean> consumer) {
        this.listen(channel, user.getIdLong(), consumer);
    }

    public void listen(TextChannel channel, long user, BiFunction<User, String, Boolean> consumer) {
        this.listen(channel.getIdLong(), user, consumer);
    }

    public void listen(TextChannel channel, User user, BiFunction<User, String, Boolean> consumer) {
        this.listen(channel.getIdLong(), user.getIdLong(), consumer);
    }

    public void listen(long channelId, long userId, BiFunction<User, String, Boolean> consumer) {
        synchronized (this.listeningConsumers) {
            this.listeningConsumers.put(new Key(channelId, userId), consumer);
        }
    }

    @AllArgsConstructor
    private static class Key {

        private final long channelId;
        private final long userId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (channelId != key.channelId) return false;
            return userId == key.userId;
        }

        @Override
        public int hashCode() {
            int result = (int) (channelId ^ (channelId >>> 32));
            result = 31 * result + (int) (userId ^ (userId >>> 32));
            return result;
        }
    }

}
