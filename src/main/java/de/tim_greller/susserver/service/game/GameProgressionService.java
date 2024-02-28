package de.tim_greller.susserver.service.game;

import de.tim_greller.susserver.events.ComponentTestsActivatedEvent;
import de.tim_greller.susserver.events.GameStartedEvent;
import de.tim_greller.susserver.persistence.entity.ComponentStatusEntity;
import de.tim_greller.susserver.persistence.entity.GameProgressionEntity;
import de.tim_greller.susserver.persistence.entity.UserGameProgressionEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import de.tim_greller.susserver.persistence.repository.GameProgressionRepository;
import de.tim_greller.susserver.persistence.repository.UserGameProgressionRepository;
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
    private final UserRepository userRepository;
    private final UserService userService;

    private UserKey user;


    // Instantiated by the Spring IoC container during startup even if not injected anywhere.
    // It then registers itself as a handler for the ComponentTestsActivatedEvent.
    GameProgressionService(EventService eventService, UserGameProgressionRepository userGameProgressionRepository,
                           GameProgressionRepository gameProgressionRepository,
                           ComponentStatusService componentStatusService, UserRepository userRepository,
                           UserService userService) {
        this.userGameProgressionRepository = userGameProgressionRepository;
        this.gameProgressionRepository = gameProgressionRepository;
        this.componentStatusService = componentStatusService;
        this.userRepository = userRepository;
        this.userService = userService;

        eventService.registerHandler(GameStartedEvent.class, this::handleGameStarted);
        eventService.registerHandler(ComponentTestsActivatedEvent.class, this::handleComponentTestsActivated);
    }

    public void handleComponentTestsActivated(ComponentTestsActivatedEvent event) {
        componentStatusService.handleComponentTestsActivated(event);
        gameLoop();
    }

    private void handleGameStarted(GameStartedEvent gameStartedEvent) {
        resetGameProgression();
        gameLoop();
    }

    private void gameLoop() {
        GameProgressionEntity gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow()
                .getGameProgression();
        String componentName = gameProgression.getComponent().getName();
        ComponentStatusEntity currentComponentStatus = componentStatusService.getComponentStatus(
                componentName, currentUser().getUser().getEmail());

        if (currentComponentStatus.isTestsActivated()) {
            int waitDurationSeconds = gameProgression.getDelaySeconds();
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
        userGameProgressionRepository.save(UserGameProgressionEntity.builder()
                .gameProgression(gameProgressionRepository.getReferenceById(1))
                .user(currentUser())
                .build()
        );
    }

    private UserKey currentUser() {
        if (user == null) {
            user = new UserKey(userRepository.getReferenceById(userService.requireCurrentUserId()));
        }
        return user;
    }
}
