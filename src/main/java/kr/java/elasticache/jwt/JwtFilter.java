package kr.java.elasticache.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.java.elasticache.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public JwtFilter(TokenProvider tokenProvider, JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = resolveToken(request, jwtProperties.getCookie().getAccessTokenName());
        String requestURI = request.getRequestURI();

        // 1. 액세스 토큰 유효성 검사
        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            // 2. 액세스 토큰이 없거나 유효하지 않은 경우, 리프레시 토큰 확인
            String refreshToken = resolveToken(request, jwtProperties.getCookie().getRefreshTokenName());
            if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                logger.debug("액세스 토큰이 만료되었거나 없습니다. 리프레시 토큰을 사용하여 재발급을 시도합니다.");

                // 리프레시 토큰으로 인증 정보 조회
                Authentication authentication = tokenProvider.getAuthentication(refreshToken);

                // 새로운 액세스 토큰 발급
                String newAccessToken = tokenProvider.createAccessToken(authentication);

                // 응답 쿠키에 새로운 액세스 토큰 설정
                CookieUtils.addCookie(response, jwtProperties.getCookie().getAccessTokenName(), newAccessToken, jwtProperties.getAccessTokenValidityInSeconds());

                // Security Context 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("액세스 토큰이 재발급되었습니다. 사용자: {}", authentication.getName());
            } else {
                logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request, String cookieName) {
        // 1. Authorization 헤더에서 토큰 추출 시도 (액세스 토큰인 경우에만)
        if (jwtProperties.getCookie().getAccessTokenName().equals(cookieName)) {
            String bearerToken = request.getHeader(jwtProperties.getHeader());
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }

        // 2. 쿠키에서 토큰 추출 시도
        return CookieUtils.getCookieValue(request, cookieName);
    }
}
