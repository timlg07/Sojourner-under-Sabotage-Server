package de.tim_greller.susserver.events;

import de.tim_greller.susserver.dto.UserGameProgressionDTO;
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
public class GameProgressionChangedEvent extends Event {
    UserGameProgressionDTO progression;
}
