package de.tim_greller.susserver.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Jacksonized
@Builder
public class ComponentTestsExtendedEvent extends Event {

    private final String componentName;
    private final String addedTestMethodName;
}
