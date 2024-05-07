package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SurveyController {

    private static final String SURVEY_URL = "https://docs.google.com/forms/d/e/1FAIpQLSe7ZGVMqGEVhLv-3b7WaindB0g71J-rfisdq8WIEQkpIw1MvQ/viewform?usp=pp_url&entry.784200635=";
    private final UserService userService;

    @GetMapping("/survey")
    public String showSurvey() {
        String userName = userService.requireCurrentUserId();
        return "redirect:" + SURVEY_URL + userName;
    }

}
