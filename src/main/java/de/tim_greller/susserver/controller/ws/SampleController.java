package de.tim_greller.susserver.controller.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SampleController {
    @MessageMapping("/user")
    @SendTo("/topic/user")
    public Object getUser(String u) {

        return new Object() {
            String msg = "Hello, " + u + "!";
        };
    }
}
