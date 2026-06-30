package com.shubham.flashsale.idempotency;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofHours(24);
    private final ObjectMapper objectMapper;


    public boolean tryAcquire(String key){
        IdempotencyRecord record = IdempotencyRecord.builder()
                .state(IdempotencyState.PROCESSING)
                .build();

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                redisKey(key),
                serialize(record),
                TTL
        );
        return Boolean.TRUE.equals(acquired);
    }

    public void markCompleted(String key,
                              String response,
                              int status){

        IdempotencyRecord record = IdempotencyRecord.builder()
                .responseBody(response)
                .statusCode(status)
                .state(IdempotencyState.COMPLETED)
                .build();

        redisTemplate.opsForValue().set(
                redisKey(key),
                serialize(record),
                        TTL
        );

    }
    public Optional<IdempotencyRecord> get(String key){
        String value = redisTemplate.opsForValue().get(redisKey(key));
        if (value == null) {
         return Optional.empty();
        }

        return Optional.of(deserialize(value));
    }

    public void delete(String key) {
        redisTemplate.delete(redisKey(key));
    }

    private String redisKey(String key) {
        return "idem:" + key;
    }

    private String serialize(IdempotencyRecord record){
        try{
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize idempotency record",e);
        }
    }

    private IdempotencyRecord deserialize(String json) {
        try {
            return objectMapper.readValue(json, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize idempotency record", e);
        }
    }

    public boolean exists(String key) {
        return redisTemplate.hasKey(redisKey(key));
    }

}