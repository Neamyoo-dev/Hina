/*
 * Hina Client
 * Copyright (C) 2026 Hina Client
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hinaclient.hina.event;

import com.hinaclient.hina.MioHr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Eatgrapes
 * @link github.com/Eatgrapes
 */
public class EventBus {
    public static final EventBus INSTANCE = new EventBus();
    private final Map<Class<? extends Event>, List<EventSubscriber>> subscriberMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void register(Object subscriber) {
        Method[] methods = subscriber.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(EventListener.class)) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1 && Event.class.isAssignableFrom(parameters[0])) {
                    Class<? extends Event> eventClass = (Class<? extends Event>) parameters[0];
                    method.setAccessible(true);

                    Consumer<Event> consumer = event -> {
                        try {
                            method.invoke(subscriber, event);
                        } catch (Exception e) {
                            MioHr.getINSTANCE().getLogger().error("Error invoking event handler", e);
                        }
                    };

                    subscriberMap.computeIfAbsent(eventClass, k -> new ArrayList<>())
                            .add(new EventSubscriber(subscriber, consumer));
                }
            }
        }
    }

    public void unregister(Object subscriber) {
        for (List<EventSubscriber> subscribers : subscriberMap.values()) {
            subscribers.removeIf(s -> s.owner == subscriber);
        }
    }

    public void post(Event event) {
        List<EventSubscriber> eventSubscribers = subscriberMap.get(event.getClass());
        if (eventSubscribers != null) {
            for (EventSubscriber sub : new ArrayList<>(eventSubscribers)) {
                if (event.isCancelled()) {
                    break;
                }
                sub.consumer.accept(event);
            }
        }
    }

    public boolean isregister(Object subscriber) {
        return subscriberMap.values().stream()
                .anyMatch(list -> list.stream()
                        .anyMatch(s -> s.owner == subscriber));
    }

    private record EventSubscriber(Object owner, Consumer<Event> consumer) {
    }
}