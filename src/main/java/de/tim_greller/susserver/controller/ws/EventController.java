package de.tim_greller.susserver.controller.ws;

import de.tim_greller.susserver.events.Event;
import de.tim_greller.susserver.events.RoomUnlockedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EventController {

    @MessageMapping("/events")  // complete endpoint depends on configured application message handler prefix, e.g. /app/hello
    @SendTo("/topic/events")
    public Event handleClientEvent(Event clientEvent) throws Exception {
        log.info("client event [{}] timestamp: {}", clientEvent.getClass(), clientEvent.getTimestamp());
        Thread.sleep((int)(Math.random()*2000)); // simulated delay
        var serverEvent = new RoomUnlockedEvent(1L);
        log.info("server event timestamp: {}", serverEvent.getTimestamp());
        return serverEvent;
    }

}
