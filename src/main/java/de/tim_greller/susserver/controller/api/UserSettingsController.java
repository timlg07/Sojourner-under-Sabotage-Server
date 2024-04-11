package de.tim_greller.susserver.controller.api;

import de.tim_greller.susserver.dto.UserSettingsDTO;
import de.tim_greller.susserver.service.game.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping("${paths.api}/settings")
    public UserSettingsDTO getUserSettings() {
        return userSettingsService.getUserSettings();
    }

    @PutMapping("${paths.api}/settings")
    public void updateUserSettings(@RequestBody UserSettingsDTO userSettingsDTO) {
        log.info("Updating user settings: {}", userSettingsDTO);
        userSettingsService.updateUserSettings(userSettingsDTO);
    }
}
