package kr.java.elasticache.controller;

import kr.java.elasticache.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        model.addAttribute("refreshTokens", refreshTokenRepository.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/logout-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String logoutUser(@RequestParam String username) {
        refreshTokenRepository.deleteById(username);
        return "redirect:/admin/dashboard";
    }
}
