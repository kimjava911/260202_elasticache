package kr.java.elasticache.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.java.elasticache.config.JwtProperties;
import kr.java.elasticache.dto.LoginDto;
import kr.java.elasticache.dto.UserDto;
import kr.java.elasticache.jwt.CookieUtils;
import kr.java.elasticache.jwt.TokenProvider;
import kr.java.elasticache.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    @PostMapping("/authenticate")
    public String authorize(@ModelAttribute LoginDto loginDto, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        CookieUtils.addCookie(response, jwtProperties.getCookie().getAccessTokenName(), accessToken, jwtProperties.getAccessTokenValidityInSeconds());
        CookieUtils.addCookie(response, jwtProperties.getCookie().getRefreshTokenName(), refreshToken, jwtProperties.getRefreshTokenValidityInSeconds());

        return "redirect:/";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute UserDto userDto) {
        userService.signup(userDto);
        return "redirect:/login";
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String handleBadCredentialsException(BadCredentialsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
        return "redirect:/login";
    }
}
