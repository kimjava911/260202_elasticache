package kr.java.elasticache.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.java.elasticache.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final JwtProperties jwtProperties;

    @Value("${app.name}")
    private String appName;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @GetMapping("/my-page")
    public String myPage(HttpServletRequest request, Model model) {
        model.addAttribute("appName", appName);

        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> jwtProperties.getCookie().getAccessTokenName().equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> model.addAttribute("accessToken", cookie.getValue()));

            Arrays.stream(request.getCookies())
                    .filter(cookie -> jwtProperties.getCookie().getRefreshTokenName().equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> model.addAttribute("refreshToken", cookie.getValue()));
        }
        return "my-page";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "403";
    }
}
