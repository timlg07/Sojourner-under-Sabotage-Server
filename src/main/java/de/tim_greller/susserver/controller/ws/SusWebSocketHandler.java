package de.tim_greller.susserver.controller.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SusWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Handle new session established actions
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle incoming messages
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Handle session close actions
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // Log the error and handle it
    }
}
