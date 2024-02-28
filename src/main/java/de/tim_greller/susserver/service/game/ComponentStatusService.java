package de.tim_greller.susserver.service.game;

import java.util.List;

import de.tim_greller.susserver.dto.TestExecutionResultDTO;
import de.tim_greller.susserver.dto.TestSourceDTO;
import de.tim_greller.susserver.dto.TestStatus;
import de.tim_greller.susserver.events.ComponentDestroyedEvent;
import de.tim_greller.susserver.events.ComponentTestsActivatedEvent;
import de.tim_greller.susserver.events.Event;
import de.tim_greller.susserver.events.MutatedComponentTestsFailedEvent;
import de.tim_greller.susserver.exception.ClassLoadException;
import de.tim_greller.susserver.exception.CompilationException;
import de.tim_greller.susserver.exception.NotFoundException;
import de.tim_greller.susserver.exception.TestExecutionException;
import de.tim_greller.susserver.persistence.entity.ActivePatchEntity;
import de.tim_greller.susserver.persistence.entity.ComponentStatusEntity;
import de.tim_greller.susserver.persistence.entity.PatchEntity;
import de.tim_greller.susserver.persistence.keys.UserComponentKey;
import de.tim_greller.susserver.persistence.repository.ActivePatchRepository;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.ComponentStatusRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.execution.ExecutionService;
import de.tim_greller.susserver.service.execution.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComponentStatusService {

    private final EventService eventService;
    private final PatchRepository patchRepository;
    private final ActivePatchRepository activePatchRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ExecutionService executionService;
    private final TestService testService;
    private final ComponentStatusRepository componentStatusRepository;
    private final ComponentRepository componentRepository;


    public ComponentStatusEntity getComponentStatus(String componentName, String userId) {
        return componentStatusRepository.findByKey(componentName, userId)
                .orElseGet(() -> {
                            ComponentStatusEntity e = ComponentStatusEntity.builder().userComponentKey(
                                    new UserComponentKey(
                                            componentRepository.findById(componentName).orElseThrow(),
                                            userRepository.findById(userId).orElseThrow()
                                    )
                            ).build();
                            return componentStatusRepository.save(e);
                        }
                );
    }

    void handleComponentTestsActivated(ComponentTestsActivatedEvent event) {
        final String componentName = event.getComponentName();
        log.info("Component tests activated for component {}", componentName);
        final ComponentStatusEntity componentStatus = getComponentStatus(componentName, userService.requireCurrentUserId());
        componentStatus.setTestsActivated(true);
        componentStatusRepository.save(componentStatus);
    }

    /**
     * Selects and apply a mutation (=patch) to the component.
     */
    void attackCut(final String componentName) {
        log.info("Component {} is being attacked", componentName);

        List<PatchEntity> patches = patchRepository.findPatchEntitiesByComponentKey_Component_Name(componentName);

        if (patches.isEmpty()) {
            log.info("No patches found for component {}", componentName);
            return;
        }

        // TODO: not a random patch, but same for everyone to make it better comparable.
        PatchEntity patch = patches.get((int) (Math.random() * patches.size()));

        UserComponentKey key = new UserComponentKey(
                patch.getComponentKey().getComponent(),
                userRepository.findById(userService.requireCurrentUserId()).orElseThrow()
        );
        activePatchRepository.save(new ActivePatchEntity(key, patch));

        // execute mutated component
        final TestExecutionResultDTO res = executeTests(componentName);
        if (res == null) {
            return;
        }

        Event e = testExecutionResultToEvent(res, componentName);
        eventService.publishEvent(e);
    }

    private TestExecutionResultDTO executeTests(final String componentName) {
        try {
            return executionService.execute(componentName, userService.requireCurrentUserId());
        } catch (CompilationException | ClassLoadException | TestExecutionException e) {
            log.error("Failed to execute tests for component {}", componentName, e);
            return null;
        } catch (NotFoundException e) {
            log.error("No CUT found for component {}", componentName, e);
            return null;
        }
    }

    private Event testExecutionResultToEvent(final TestExecutionResultDTO res, final String componentName) {
        if (res.getTestStatus() == TestStatus.FAILED) {
            log.info("Component {} tests failed", componentName);
            return new MutatedComponentTestsFailedEvent(componentName, res);
        } else {
            log.info("Component {} tests passed", componentName);
            final String userId = userService.requireCurrentUserId();
            final ComponentStatusEntity componentStatus = getComponentStatus(componentName, userId);
            testService.replaceWithAutoGeneratedTest(componentStatus, userId);
            TestSourceDTO src = testService.getOrCreateTestDtoForComponent(componentName, userId);
            return new ComponentDestroyedEvent(componentName, src);
        }
    }
}
