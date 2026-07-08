package com.shubham.flashsale.config;

import com.shubham.flashsale.flashsale.events.sse.StockUpdateSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    redisTemplate.setConnectionFactory(factory);

    StringRedisSerializer keySerializer = new StringRedisSerializer();

    GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

    redisTemplate.setKeySerializer(keySerializer);
    redisTemplate.setHashKeySerializer(keySerializer);

    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);

    return redisTemplate;
  }

  @Bean
  CommandLineRunner testRedis(StringRedisTemplate stringRedisTemplate) {

    return args -> {
      stringRedisTemplate.opsForValue().set("hello", "world");

      log.info("Redis Test Success");
    };
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory, StockUpdateSubscriber stockUpdateSubscriber) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);

    container.addMessageListener(
        (message, pattern) -> stockUpdateSubscriber.handleMessage(message.toString()),
        new PatternTopic("stock:updates:*"));

    return container;
  }
}
