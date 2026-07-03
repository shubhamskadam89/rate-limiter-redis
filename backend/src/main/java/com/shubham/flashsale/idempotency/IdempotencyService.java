package com.shubham.flashsale.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyStore store;

    public boolean tryAcquire(String key) {
        log.debug("Attempting to acquire idempotency lock for key={}", key);
        return store.tryAcquire(key);
    }

    public Optional<IdempotencyRecord> get(String key) {
        log.debug("Fetching idempotency record for key={}", key);
        return store.get(key);
    }

    public void complete(String key,
                         String responseBody,
                         int statusCode) {

        log.debug("Marking idempotency key={} as completed with statusCode={}", key, statusCode);
        store.markCompleted(key, responseBody, statusCode);
    }

    public void release(String key) {
        log.debug("Releasing idempotency key={}", key);
        store.delete(key);
    }
}