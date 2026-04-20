package com.photo.act.photo_act.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis + Cache configuration.
 *
 * Uses GenericJackson2JsonRedisSerializer (JSON) instead of Java serialization —
 * human-readable in redis-cli, resilient across app restarts.
 *
 * ── Cache TTL table ───────────────────────────────────────────────────────────
 *  og-meta          1 hour    — OG metadata (article, photo, album, etc.)
 *  og-meta-profile  24 hours  — Photographer profiles (change rarely)
 *  og-meta-event    15 min    — Events (update frequently near start date)
 *  news-item        30 min    — Single NewsDto by id
 *  news-list        10 min    — Paged news lists (by category / latest)
 *  news-categories   5 min    — All categories with computed stats
 */
@Configuration
@EnableCaching
public class RedisConfig {

    private static final Duration DEFAULT_TTL      = Duration.ofHours(1);
    private static final Duration PROFILE_TTL      = Duration.ofHours(24);
    private static final Duration EVENT_TTL        = Duration.ofMinutes(15);

    // News section TTLs
    private static final Duration NEWS_ITEM_TTL    = Duration.ofMinutes(30);
    private static final Duration NEWS_LIST_TTL    = Duration.ofMinutes(10);
    private static final Duration NEWS_CAT_TTL     = Duration.ofMinutes(5);

    @Bean
    public GenericJackson2JsonRedisSerializer redisSerializer() {
        // ObjectMapper is created locally — NOT exposed as a Spring bean.
        // Exposing it would make JacksonAutoConfiguration's @ConditionalOnMissingBean
        // skip creating the primary ObjectMapper, causing all HTTP JSON serialization
        // to use this Redis-specific mapper (with activateDefaultTyping).
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        // Serialize dates as ISO strings ("2024-04-19T13:30:00"), not arrays.
        // Arrays conflict with the @class wrapper that activateDefaultTyping adds
        // for non-final types like LocalDateTime, breaking deserialization on cache hit.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            GenericJackson2JsonRedisSerializer redisSerializer) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            GenericJackson2JsonRedisSerializer redisSerializer) {

        RedisSerializationContext.SerializationPair<Object> jsonPair =
                RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(jsonPair)
                .disableCachingNullValues()
                .prefixCacheNameWith("og:");

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "og-meta",         defaultConfig,
                "og-meta-profile", defaultConfig.entryTtl(PROFILE_TTL),
                "og-meta-event",   defaultConfig.entryTtl(EVENT_TTL),
                "news-item",       defaultConfig.entryTtl(NEWS_ITEM_TTL),
                "news-list",       defaultConfig.entryTtl(NEWS_LIST_TTL),
                "news-categories", defaultConfig.entryTtl(NEWS_CAT_TTL)
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
