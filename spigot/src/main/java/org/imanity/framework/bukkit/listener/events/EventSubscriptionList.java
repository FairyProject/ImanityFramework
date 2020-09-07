package org.imanity.framework.bukkit.listener.events;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventSubscriptionList extends LinkedHashMap<String, EventSubscription<?>> {

    @Override
    public EventSubscription<?> get(Object key) {
        return super.getOrDefault(key, null);
    }

    @Override
    public EventSubscription<?> remove(Object key) {
        return this.remove(key, true);
    }

    public EventSubscription<?> remove(Object key, boolean unregister) {
        EventSubscription<?> subscription = super.remove(key);
        if (unregister && subscription != null && subscription.isActive()) {
            subscription.unregister();
        }
        return subscription;
    }

    @Override
    public void clear() {

        synchronized (this) {
            for (EventSubscription<?> eventSubscription : this.values()) {
                if (eventSubscription.isActive()) {
                    eventSubscription.unregister();
                }
            }
        }

        super.clear();

    }
}
