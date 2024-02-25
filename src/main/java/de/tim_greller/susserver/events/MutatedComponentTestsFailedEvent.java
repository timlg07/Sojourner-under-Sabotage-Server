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
public class MutatedComponentTestsFailedEvent extends Event {

    private final String componentName;

    // TODO: add more info about test failure?

}
