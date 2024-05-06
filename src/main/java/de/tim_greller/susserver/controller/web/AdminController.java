package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/create-accounts")
    public String createAccounts(@Param("amount") int amount, Model model) {
        var createdUsers = adminService.createAccounts(amount);
        model.addAttribute("createdUsers", createdUsers);
        return "admin";
    }
}
