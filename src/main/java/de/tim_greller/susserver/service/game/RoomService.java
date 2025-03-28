package de.tim_greller.susserver.service.game;

import de.tim_greller.susserver.events.RoomUnlockedEvent;
import de.tim_greller.susserver.persistence.entity.RoomEntity;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.keys.UserRoomKey;
import de.tim_greller.susserver.persistence.repository.RoomRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RoomService {

    private final UserService userService;
    private final RoomRepository roomRepository;

    public RoomService(EventService eventService, UserService userService, RoomRepository roomRepository) {
        this.userService = userService;
        this.roomRepository = roomRepository;

        eventService.registerHandler(RoomUnlockedEvent.class, this::handleRoomUnlocked);
    }

    private void handleRoomUnlocked(RoomUnlockedEvent event) {
        log.info("Room {} unlocked for user {}", event.getRoomId(), userService.requireCurrentUserId());

        final RoomEntity room = roomRepository
                .findByKey(event.getRoomId(), userService.requireCurrentUserId())
                .orElseGet(() -> {
                    final UserEntity user = userService.requireCurrentUser();
                    final UserRoomKey key = new UserRoomKey(event.getRoomId(), user);
                    return new RoomEntity(key, false);
                });

        if (!room.isUnlocked()) {
            room.setUnlocked(true);
            roomRepository.save(room);
        }
    }
}
