package de.tim_greller.susserver.events;

import lombok.Getter;

@Getter
public class Event {
    private final long timestamp = System.currentTimeMillis();
}
