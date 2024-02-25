package de.tim_greller.susserver.service.game;

import java.util.List;

import de.tim_greller.susserver.events.ComponentTestsActivatedEvent;
import de.tim_greller.susserver.events.MutatedComponentTestsFailedEvent;
import de.tim_greller.susserver.persistence.entity.ActivePatchEntity;
import de.tim_greller.susserver.persistence.entity.PatchEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import de.tim_greller.susserver.persistence.repository.ActivePatchRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComponentStatusService {

    private final EventService eventService;
    private final PatchRepository patchRepository;
    private final ActivePatchRepository activePatchRepository;
    private final UserService userService;
    private final UserRepository userRepository;


    public ComponentStatusService(EventService eventService, PatchRepository patchRepository,
                                  ActivePatchRepository activePatchRepository, UserService userService,
                                  UserRepository userRepository) {
        this.eventService = eventService;
        this.patchRepository = patchRepository;
        this.activePatchRepository = activePatchRepository;
        this.userService = userService;
        this.userRepository = userRepository;

        eventService.registerHandler(ComponentTestsActivatedEvent.class, this::handleComponentTestsActivated);
    }

    public void handleComponentTestsActivated(ComponentTestsActivatedEvent event) {
        log.info("Component tests activated for component {}", event.getComponentName());

        try {
            Thread.sleep((int)(Math.random()*10_000));
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }

        log.info("Component {} is being attacked", event.getComponentName());

        // select random patch and apply it to the component
        List<PatchEntity> patches = patchRepository.findPatchEntitiesByComponentKey_Component_Name(event.getComponentName());
        if (patches.isEmpty()) {
            log.info("No patches found for component {}", event.getComponentName());
            return;
        }
        PatchEntity patch = patches.get((int)(Math.random()*patches.size()));
        UserComponentKey key = new UserComponentKey(
                patch.getComponentKey().getComponent(),
                userRepository.findById(userService.requireCurrentUserId()).orElseThrow()
        );
        activePatchRepository.save(new ActivePatchEntity(key, patch));

        // TODO: run tests

        eventService.publishEvent(new MutatedComponentTestsFailedEvent(event.getComponentName()));
    }
}
