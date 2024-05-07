package de.tim_greller.susserver.service.game;

import de.tim_greller.susserver.dto.UserSettingsDTO;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.keys.UserKey;
import de.tim_greller.susserver.persistence.repository.UserSettingsRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserService userService;

    public UserSettingsDTO getUserSettings() {
        final UserEntity user = userService.requireCurrentUser();
        return userSettingsRepository
                .findById(new UserKey(user))
                .map(UserSettingsDTO::fromEntity)
                .orElseGet(UserSettingsDTO::new);
    }

    public void updateUserSettings(UserSettingsDTO userSettingsDTO) {
        final UserEntity user = userService.requireCurrentUser();
        userSettingsRepository.save(userSettingsDTO.toEntity(user));
    }

    public void resetUserSettings() {
        final UserEntity user = userService.requireCurrentUser();
        userSettingsRepository.deleteById(new UserKey(user));
    }
}
