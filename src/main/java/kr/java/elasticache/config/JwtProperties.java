package kr.java.elasticache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private long accessTokenValidityInSeconds;
    private long refreshTokenValidityInSeconds;
    private String header;
    private Cookie cookie;

    @Getter
    @Setter
    public static class Cookie {
        private String accessTokenName;
        private String refreshTokenName;
    }
}
