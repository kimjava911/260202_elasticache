package kr.java.elasticache.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "refreshToken", timeToLive = 604800) // 7 days
public class RefreshToken {

    @Id
    private String username;

    @Indexed
    private String token;
}
