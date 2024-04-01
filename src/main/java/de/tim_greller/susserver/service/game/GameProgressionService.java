package de.tim_greller.susserver.service.game;

import de.tim_greller.susserver.events.ComponentFixedEvent;
import de.tim_greller.susserver.events.ComponentTestsActivatedEvent;
import de.tim_greller.susserver.events.GameStartedEvent;
import de.tim_greller.susserver.persistence.entity.ComponentStatusEntity;
import de.tim_greller.susserver.persistence.entity.GameProgressionEntity;
import de.tim_greller.susserver.persistence.entity.UserGameProgressionEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import de.tim_greller.susserver.persistence.repository.GameProgressionRepository;
import de.tim_greller.susserver.persistence.repository.UserGameProgressionRepository;
import de.tim_greller.susserver.persistence.repository.UserModifiedCutRepository;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GameProgressionService {

    private final UserGameProgressionRepository userGameProgressionRepository;
    private final GameProgressionRepository gameProgressionRepository;
    private final ComponentStatusService componentStatusService;
    private final UserService userService;
    private final UserModifiedCutRepository userModifiedCutRepository;

    private UserKey user;


    // Instantiated by the Spring IoC container during startup even if not injected anywhere.
    // It then registers itself as a handler for the ComponentTestsActivatedEvent.
    GameProgressionService(EventService eventService, UserGameProgressionRepository userGameProgressionRepository,
                           GameProgressionRepository gameProgressionRepository,
                           ComponentStatusService componentStatusService, UserRepository userRepository,
                           UserService userService, UserModifiedCutRepository userModifiedCutRepository) {
        this.userGameProgressionRepository = userGameProgressionRepository;
        this.gameProgressionRepository = gameProgressionRepository;
        this.componentStatusService = componentStatusService;
        this.userService = userService;
        this.userModifiedCutRepository = userModifiedCutRepository;

        eventService.registerHandler(GameStartedEvent.class, this::handleGameStarted);
        eventService.registerHandler(ComponentTestsActivatedEvent.class, this::handleComponentTestsActivated);
        eventService.registerHandler(ComponentFixedEvent.class, this::handleComponentFixed);
    }

    public void handleComponentTestsActivated(ComponentTestsActivatedEvent event) {
        if (componentStatusService.handleComponentTestsActivated(event)) {
            gameLoop();
        }
    }

    private void handleGameStarted(GameStartedEvent gameStartedEvent) {
        resetGameProgression();
        gameLoop();
    }

    /**
     * Bump user game progression. Update the user component status.
     * @param componentFixedEvent the event
     */
    private void handleComponentFixed(ComponentFixedEvent componentFixedEvent) {
        var userProgress = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var progression = userProgress.getGameProgression();
        var newProgression = gameProgressionRepository.getReferenceById(progression.getOrderIndex() + 1);
        userProgress.setGameProgression(newProgression);
        // TODO: handle game finished on max level reached
        userGameProgressionRepository.save(userProgress);

        // Set the component stage
        componentStatusService.getComponentStatus(newProgression.getComponent().getName(), currentUser().getUser().getEmail())
                .setStage(newProgression.getStage());
    }

    private void gameLoop() {
        GameProgressionEntity gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow()
                .getGameProgression();
        String componentName = gameProgression.getComponent().getName();
        ComponentStatusEntity currentComponentStatus = componentStatusService.getComponentStatus(
                componentName, currentUser().getUser().getEmail());

        if (currentComponentStatus.isTestsActivated()) {
            int waitDurationSeconds = gameProgression.getDelaySeconds();
            log.info("Waiting for {} seconds before attacking component {}", waitDurationSeconds, componentName);
            try {
                Thread.sleep(waitDurationSeconds * 1_000L);
            } catch (InterruptedException e) {
                log.error("Game loop was interrupted.");
                Thread.currentThread().interrupt();
            }
            componentStatusService.attackCut(componentName);
        }
    }

    private void resetGameProgression() {
        componentStatusService.resetComponentStatus(currentUser().getUser().getEmail());
        userGameProgressionRepository.save(UserGameProgressionEntity.builder()
                .gameProgression(gameProgressionRepository.getReferenceById(1))
                .user(currentUser())
                .build()
        );
        userModifiedCutRepository.deleteAllByUserComponentKey_User_Email(currentUser().getUser().getEmail());
    }

    private UserKey currentUser() {
        if (user == null) {
            user = new UserKey(userService.requireCurrentUser());
        }
        return user;
    }
}
