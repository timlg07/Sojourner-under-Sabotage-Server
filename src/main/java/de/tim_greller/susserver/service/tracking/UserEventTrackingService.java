package de.tim_greller.susserver.service.tracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import de.tim_greller.susserver.persistence.entity.UserEventTrackingEntity;
import de.tim_greller.susserver.persistence.repository.UserEventTrackingRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventTrackingService {

    private final UserEventTrackingRepository userEventTrackingRepository;
    private final UserService userService;

    public void trackEvent(String eventType, Object details) {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event details", e);
        }
        userEventTrackingRepository.save(UserEventTrackingEntity.builder()
                .user(userService.requireCurrentUser())
                .timestamp(System.currentTimeMillis())
                .eventType(eventType)
                .json(json)
                .build());
    }

}
