package de.tim_greller.susserver.controller.ws;

import java.security.Principal;

import de.tim_greller.susserver.events.Event;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.game.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @MessageMapping("/events")  // complete endpoint depends on configured application message handler prefix: /app/events
    public void handleClientEvent(Event clientEvent, Principal principal) {
        log.info("client event [{}], timestamp: {}, user: {}",
                clientEvent.getClass().getSimpleName(),
                clientEvent.getTimestamp(),
                principal.getName()
        );

        // Spring security context is not available for STOMP messages
        userService.overridePrincipal(principal);

        eventService.handleEvent(clientEvent);
    }

}
