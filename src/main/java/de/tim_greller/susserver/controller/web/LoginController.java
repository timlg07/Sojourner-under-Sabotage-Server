package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.UserService;
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

    @GetMapping("/login")
    public String login() {
        if (userService.getCurrentUserId().isPresent()) {
            return "redirect:/game";
        } else {
            return "login";
        }
    }
}
