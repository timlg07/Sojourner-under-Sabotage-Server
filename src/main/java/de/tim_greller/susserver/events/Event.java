package de.tim_greller.susserver.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public class Event {
    private final long timestamp = System.currentTimeMillis();
}
