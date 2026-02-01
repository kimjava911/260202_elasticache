package kr.java.elasticache.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.java.elasticache.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);
    private static final String AUTHORITIES_KEY = "auth";

    private final SecretKey key;
    private final JwtProperties jwtProperties;

    public TokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + jwtProperties.getAccessTokenValidityInSeconds() * 1000);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key)
                .expiration(validity)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + jwtProperties.getRefreshTokenValidityInSeconds() * 1000);

        // Refresh Token에는 권한 정보(auth claim)를 포함하지 않음 (또는 필요 시 포함)
        // 여기서는 Access Token 재발급 시 권한 정보를 DB 등에서 다시 조회하거나,
        // Refresh Token에도 권한 정보를 포함해야 함.
        // 현재 로직상 getAuthentication에서 claims.get(AUTHORITIES_KEY)를 호출하므로 포함해야 함.
        
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities) // 권한 정보 추가
                .signWith(key)
                .expiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }
}
