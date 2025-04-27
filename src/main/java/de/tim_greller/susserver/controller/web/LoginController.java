package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.game.GameProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Returns the login view for unauthenticated users. Redirects to the game if you're already logged in.
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final GameProgressionService gameProgressionService;

    @GetMapping("/login")
    public String login() {
        var u = userService.getCurrentUserId();
        if (u.isPresent()) {
            // initialize game state
            if (gameProgressionService.getCurrentGameProgression().isEmpty()) {
                gameProgressionService.resetGameProgression();
            }

            // redirect
            if (u.get().equals("admin")) {
                return "redirect:/admin";
            } else {
                return "redirect:/game";
            }
        } else {
            return "login";
        }
    }
}
