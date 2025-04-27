package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.game.GameProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ResetController {

    private final GameProgressionService gameProgressionService;

    @GetMapping("/reset")
    public String newGame() {
        gameProgressionService.resetGameProgression();
        return "redirect:/game";
    }
}
