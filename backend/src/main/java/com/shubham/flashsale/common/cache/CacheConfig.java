package com.shubham.flashsale.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {

        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(serializer)
                        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(
                        Map.of(
                                CacheNames.PRODUCTS,
                                defaultConfig.entryTtl(CacheTtl.PRODUCTS),

                                CacheNames.PRODUCT,
                                defaultConfig.entryTtl(CacheTtl.PRODUCT),

                                CacheNames.SALE,
                                defaultConfig.entryTtl(CacheTtl.SALE),

                                CacheNames.SALE_DETAIL,
                                defaultConfig.entryTtl(CacheTtl.SALE_DETAIL),

                                CacheNames.ADMIN_SALES,
                                defaultConfig.entryTtl(CacheTtl.ADMIN_SALES),

                                CacheNames.AVAILABLE_SALES,
                                defaultConfig.entryTtl(CacheTtl.AVAILABLE_SALES),

                                CacheNames.SALE_ITEMS,
                                defaultConfig.entryTtl(CacheTtl.SALE_ITEMS)
                        )
                )
                .build();
    }
}