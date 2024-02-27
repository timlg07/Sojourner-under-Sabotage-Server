package de.tim_greller.susserver.service.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.tim_greller.susserver.events.Event;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventService {

    /**
     * Handlers that handle events coming from the client.
     */
    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> eventHandlers = new HashMap<>();

    /**
     * Publishes an event to the client.
     */
    @Setter
    private Consumer<Event> eventPublisher;

    public <T extends Event> void registerHandler(@NonNull Class<T> eventType, @NonNull Consumer<T> handler) {
        eventHandlers.computeIfAbsent(eventType, key -> new ArrayList<>(1));
        eventHandlers.get(eventType).add(handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void handleEvent(@NonNull T event) {
        Class<T> eventType = (Class<T>) event.getClass();
        if (eventHandlers.containsKey(eventType)) {
            List<Consumer<? extends Event>> handler = eventHandlers.get(eventType);
            for (Consumer<? extends Event> consumer : handler) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }

    public void publishEvent(@NonNull Event event) {
        if (eventPublisher != null) {
            eventPublisher.accept(event);
        } else {
            log.error("No event publisher set, cannot publish event {}", event.getClass().getSimpleName());
        }
    }
}
