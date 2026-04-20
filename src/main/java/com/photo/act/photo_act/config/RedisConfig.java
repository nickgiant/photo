package com.photo.act.photo_act.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration.
 *
 * Cache management (TTL, prefix, serializer) is driven by the
 * spring.cache.redis.* properties in application.properties so that
 * this class stays in sync with the user's existing Redis setup.
 *
 * The custom RedisTemplate<String, Object> is only used by NewsService
 * for raw view/like counters (StringRedisTemplate also works — both beans
 * are auto-configured by Spring Boot; this one is kept for explicit typing).
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
