package de.tim_greller.susserver.service.game;

import java.util.List;
import java.util.Objects;

import static de.tim_greller.susserver.dto.GameProgressStatus.DEBUGGING;
import static de.tim_greller.susserver.dto.GameProgressStatus.DESTROYED;
import static de.tim_greller.susserver.dto.GameProgressStatus.DOOR;
import static de.tim_greller.susserver.dto.GameProgressStatus.MUTATED;
import static de.tim_greller.susserver.dto.GameProgressStatus.TALK;
import static de.tim_greller.susserver.dto.GameProgressStatus.TEST;
import static de.tim_greller.susserver.dto.GameProgressStatus.TESTS_ACTIVE;

import de.tim_greller.susserver.dto.UserGameProgressionDTO;
import de.tim_greller.susserver.events.ComponentDestroyedEvent;
import de.tim_greller.susserver.events.ComponentFixedEvent;
import de.tim_greller.susserver.events.ComponentTestsActivatedEvent;
import de.tim_greller.susserver.events.ConversationFinishedEvent;
import de.tim_greller.susserver.events.DebugStartEvent;
import de.tim_greller.susserver.events.GameFinishedEvent;
import de.tim_greller.susserver.events.GameProgressionChangedEvent;
import de.tim_greller.susserver.events.GameStartedEvent;
import de.tim_greller.susserver.events.MutatedComponentTestsFailedEvent;
import de.tim_greller.susserver.events.RoomUnlockedEvent;
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
    private final EventService eventService;
    private final UserSettingsService userSettingsService;
    private final UserModifiedCutRepository userModifiedCutRepository;


    // Instantiated by the Spring IoC container during startup even if not injected anywhere.
    // It then registers itself as a handler for the ComponentTestsActivatedEvent.
    GameProgressionService(EventService eventService, UserGameProgressionRepository userGameProgressionRepository,
                           GameProgressionRepository gameProgressionRepository,
                           ComponentStatusService componentStatusService, UserRepository userRepository,
                           UserService userService, UserSettingsService userSettingsService, UserModifiedCutRepository userModifiedCutRepository) {
        this.userGameProgressionRepository = userGameProgressionRepository;
        this.gameProgressionRepository = gameProgressionRepository;
        this.componentStatusService = componentStatusService;
        this.userService = userService;
        this.eventService = eventService;
        this.userSettingsService = userSettingsService;
        this.userModifiedCutRepository = userModifiedCutRepository;

        eventService.registerHandler(GameStartedEvent.class, this::handleGameStarted);
        eventService.registerHandler(RoomUnlockedEvent.class, this::handleRoomUnlocked);
        eventService.registerHandler(ConversationFinishedEvent.class, this::handleConversationFinished);
        eventService.registerHandler(ComponentTestsActivatedEvent.class, this::handleComponentTestsActivated);
        eventService.registerHandler(ComponentDestroyedEvent.class, this::handleComponentDestroyed);
        eventService.registerHandler(MutatedComponentTestsFailedEvent.class, this::handleMutatedComponentTestsFailed);
        eventService.registerHandler(DebugStartEvent.class, this::handleDebugStart);
        eventService.registerHandler(ComponentFixedEvent.class, this::handleComponentFixed);
    }

    public void handleComponentTestsActivated(ComponentTestsActivatedEvent event) {
        UserGameProgressionEntity userGameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        if (userGameProgression.getStatus() != TEST) {
            log.warn("Received ComponentTestsActivatedEvent while not in TEST state.");
            return;
        }
        if (componentStatusService.handleComponentTestsActivated(event)) {
            userGameProgression.setStatus(TESTS_ACTIVE);
            userGameProgressionRepository.save(userGameProgression);
            changeGameProgression(userGameProgression);
            gameLoop();
        }
    }

    private void handleGameStarted(GameStartedEvent gameStartedEvent) {
        // handle TESTS_ACTIVE state
        gameLoop();

        // handle DESTROYED and MUTATED states
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        if (List.of(DESTROYED, MUTATED).contains(gameProgression.getStatus())) {
            // RESET game progression to TEST_ACTIVE, so that test failures will trigger
            gameProgression.setStatus(TESTS_ACTIVE);
            userGameProgressionRepository.save(gameProgression);
            componentStatusService.attackCut(gameProgression.getGameProgression().getComponent().getName());
        } else {
            // send initial game progression to the client (DOOR, TALK, TEST, DEBUGGING)
            changeGameProgression(userGameProgressionRepository.findById(currentUser()).orElseThrow());
        }
    }

    /**
     * Bump user game progression. Update the user component status.
     * @param componentFixedEvent the event
     */
    private void handleComponentFixed(ComponentFixedEvent componentFixedEvent) {
        var userProgress = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var progression = userProgress.getGameProgression();

        if (userProgress.getStatus() != DEBUGGING) {
            log.error("Received ComponentFixedEvent while not in DEBUGGING state.");
            return;
        }

        if (!Objects.equals(progression.getComponent().getName(), componentFixedEvent.getComponentName())) {
            log.error("Received ComponentFixedEvent for wrong component.");
            return;
        }

        var newProgressionOpt = gameProgressionRepository.findById(progression.getOrderIndex() + 1);
        if (newProgressionOpt.isEmpty()) {
            // TODO: handle game finished on max level reached
            eventService.publishEvent(new GameFinishedEvent());
            return;
        }
        var newProgression = newProgressionOpt.get();

        userProgress.setGameProgression(newProgression);
        userProgress.setStatus(newProgression.getStage() == 1 ? DOOR : TESTS_ACTIVE);
        userGameProgressionRepository.save(userProgress);
        changeGameProgression(userProgress);

        // Set the component stage
        componentStatusService.getComponentStatus(newProgression.getComponent().getName(), currentUser().getUser().getUsername())
                .setStage(newProgression.getStage());
    }

    private void handleRoomUnlocked(RoomUnlockedEvent roomUnlockedEvent) {
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var roomIdMatches = gameProgression.getGameProgression().getRoomId() == roomUnlockedEvent.getRoomId();
        if (gameProgression.getStatus() == DOOR && roomIdMatches) {
            gameProgression.setStatus(TALK);
            userGameProgressionRepository.save(gameProgression);
            changeGameProgression(gameProgression);
        }
    }

    private void handleConversationFinished(ConversationFinishedEvent conversationFinishedEvent) {
        UserGameProgressionEntity userGameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        if (userGameProgression.getStatus() == TALK) {
            userGameProgression.setStatus(TEST);
            userGameProgressionRepository.save(userGameProgression);
            changeGameProgression(userGameProgression);
        }
    }

    private void handleMutatedComponentTestsFailed(MutatedComponentTestsFailedEvent mutatedComponentTestsFailedEvent) {
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var componentMatches = gameProgression.getGameProgression().getComponent().getName().equals(mutatedComponentTestsFailedEvent.getComponentName());
        if (gameProgression.getStatus() == TESTS_ACTIVE && componentMatches) {
            gameProgression.setStatus(MUTATED);
            userGameProgressionRepository.save(gameProgression);
            changeGameProgression(gameProgression);
            System.out.println("Component mutated: " + mutatedComponentTestsFailedEvent.getComponentName());
        }
        System.out.println("// Component mutated: " + mutatedComponentTestsFailedEvent.getComponentName());
    }

    private void handleComponentDestroyed(ComponentDestroyedEvent componentDestroyedEvent) {
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var componentMatches = gameProgression.getGameProgression().getComponent().getName().equals(componentDestroyedEvent.getComponentName());
        if (gameProgression.getStatus() == TESTS_ACTIVE && componentMatches) {
            gameProgression.setStatus(DESTROYED);
            userGameProgressionRepository.save(gameProgression);
            changeGameProgression(gameProgression);
            System.out.println("Component destroyed: " + componentDestroyedEvent.getComponentName());
        }
        System.out.println("// Component destroyed: " + componentDestroyedEvent.getComponentName());
    }

    private void handleDebugStart(DebugStartEvent debugStartEvent) {
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        var componentMatches = gameProgression.getGameProgression().getComponent().getName().equals(debugStartEvent.getComponentName());
        if (gameProgression.getStatus().readyForDebugging() && componentMatches) {
            gameProgression.setStatus(DEBUGGING);
            userGameProgressionRepository.save(gameProgression);
            changeGameProgression(gameProgression);
        }
    }

    private void gameLoop() {
        var gameProgression = userGameProgressionRepository.findById(currentUser()).orElseThrow();
        if (gameProgression.getStatus() == TESTS_ACTIVE) {
            String componentName = gameProgression.getGameProgression().getComponent().getName();
            int waitDurationSeconds = gameProgression.getGameProgression().getDelaySeconds();
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

    public void resetGameProgression() {
        componentStatusService.resetComponentStatus(currentUser().getUser().getUsername());
        var gameProgression = UserGameProgressionEntity.builder()
                .gameProgression(gameProgressionRepository.getReferenceById(1))
                .status(TALK)
                .user(currentUser())
                .build();
        userGameProgressionRepository.save(gameProgression);
        userModifiedCutRepository.deleteByUserId(currentUser().getUser().getUsername());
        userSettingsService.resetUserSettings();
    }

    private UserKey currentUser() {
        return new UserKey(userService.requireCurrentUser());
    }

    private void changeGameProgression(UserGameProgressionEntity ugp) {
        var ugpDto = UserGameProgressionDTO.builder()
                .id(ugp.getGameProgression().getOrderIndex())
                .room(ugp.getGameProgression().getRoomId())
                .componentName(ugp.getGameProgression().getComponent().getName())
                .stage(ugp.getGameProgression().getStage())
                .status(ugp.getStatus())
                .build();
        eventService.publishEvent(new GameProgressionChangedEvent(ugpDto));
    }
}
