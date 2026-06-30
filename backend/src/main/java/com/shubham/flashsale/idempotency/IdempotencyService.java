package com.shubham.flashsale.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyStore store;

    public boolean tryAcquire(String key) {
        return store.tryAcquire(key);
    }

    public Optional<IdempotencyRecord> get(String key) {
        return store.get(key);
    }

    public void complete(String key,
                         String responseBody,
                         int statusCode) {

        store.markCompleted(key, responseBody, statusCode);
    }

    public void release(String key) {
        store.delete(key);
    }
}