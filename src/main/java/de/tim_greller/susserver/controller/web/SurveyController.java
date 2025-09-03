package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.UserService;
import de.tim_greller.susserver.service.tracking.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SurveyController {

    private final UserService userService;
    private final SurveyService surveyService;

    @GetMapping("/survey")
    public String showSurvey() {
        String userName = userService.requireCurrentUserId();
        String surveyUrl = surveyService.getSurveyUrl();

        if (surveyUrl == null) {
            return "/error";
        } else {
            return "redirect:" + surveyUrl + userName;
        }
    }

}
