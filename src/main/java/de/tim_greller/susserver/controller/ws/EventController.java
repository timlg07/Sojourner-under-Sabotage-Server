package de.tim_greller.susserver.controller.ws;

import java.security.Principal;

import de.tim_greller.susserver.events.Event;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.game.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public EventController(EventService eventService, UserService userService,
                           SimpMessagingTemplate simpMessagingTemplate) {
        this.eventService = eventService;
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;

        eventService.setEventPublisher(this::sendEventToClient);
    }

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

    public void sendEventToClient(Event event) {
        final String username = userService.requireCurrentUserId();
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/events", event);
        log.info("sent event [{}] to user {}", event.getClass().getSimpleName(), username);
    }

}
