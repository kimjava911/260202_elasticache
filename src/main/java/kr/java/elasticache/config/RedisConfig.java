package kr.java.elasticache.config;

import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
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
        
        if (redisURI.getPassword() != null) {
            configuration.setPassword(new String(redisURI.getPassword()));
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        if (redisURI.isSsl()) {
            factory.setUseSsl(true);
        }
        
        return factory;
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
