package de.tim_greller.susserver.controller.web;

import de.tim_greller.susserver.service.auth.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/create-accounts")
    public String createAccounts(@Param("amount") int amount, RedirectAttributes redirectAttributes) {
        var createdUsers = adminService.createAccounts(amount);
        redirectAttributes.addFlashAttribute("createdUsers", createdUsers);
        return "redirect:/admin";
    }
}
