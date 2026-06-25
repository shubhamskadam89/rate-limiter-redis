package com.shubham.flashsale.idempotency;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private final StringRedisTemplate redisTemplate;
    private static final Duration ttl = Duration.ofHours(24);
    private final ObjectMapper objectMapper;

    public boolean tryAcquire(String key){
        return false;
    }

    public void markCompleted(String key,
                              String response,
                              HttpStatus status){

    }
    public Optional<IdempotencyRecord> get(String key){
        return Optional.empty();
    }

    public void delete(String key) {

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

}