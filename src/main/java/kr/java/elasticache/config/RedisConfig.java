package kr.java.elasticache.config;

import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisURI redisURI = RedisURI.create(redisUrl);

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisURI.getHost());
        configuration.setPort(redisURI.getPort());

        // getPassword() deprecated 대응 (Lettuce 6.2+)
        RedisCredentials credentials = redisURI.getCredentialsProvider().resolveCredentials().block();
        if (credentials != null && credentials.hasPassword()) {
            configuration.setPassword(RedisPassword.of(new String(credentials.getPassword())));
        }

        // SSL 사용 여부에 따라 클라이언트 설정 구성
        LettuceClientConfiguration clientConfig;
        if (redisURI.isSsl()) {
            clientConfig = LettuceClientConfiguration.builder()
                    .useSsl()
                    .build();
        } else {
            clientConfig = LettuceClientConfiguration.builder().build();
        }

        return new LettuceConnectionFactory(configuration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
