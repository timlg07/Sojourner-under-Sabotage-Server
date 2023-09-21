package de.tim_greller.susserver.controller;

import de.tim_greller.susserver.dto.UserDTO;
import de.tim_greller.susserver.exception.UserAlreadyExistException;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RegistrationController {

    private final  UserService userService;

    public RegistrationController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(WebRequest request, Model model) {
        UserDTO userDto = new UserDTO();
        model.addAttribute("user", userDto);
        return "register";
    }

    @PostMapping("/register")
    public ModelAndView registerUserAccount(@ModelAttribute("user") UserDTO userDto, ModelAndView mav) {
        try {
            UserEntity registered = userService.registerNewUserAccount(userDto);
            return new ModelAndView("home", "user", registered);
        } catch (UserAlreadyExistException uaeEx) {
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }
    }
}
