package com.example.mvp.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration BLOCK_TIME = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        String normalized = normalize(username);
        AttemptState state = attempts.get(normalized);
        if (state == null) {
            return false;
        }
        Instant now = Instant.now();
        if (state.blockedUntil != null && now.isBefore(state.blockedUntil)) {
            return true;
        }
        if (state.blockedUntil != null && !now.isBefore(state.blockedUntil)) {
            attempts.remove(normalized);
        }
        return false;
    }

    public void registerFailedAttempt(String username) {
        String normalized = normalize(username);
        Instant now = Instant.now();
        attempts.compute(normalized, (key, state) -> {
            AttemptState current = state == null ? new AttemptState() : state;
            if (current.blockedUntil != null && now.isBefore(current.blockedUntil)) {
                return current;
            }
            if (current.blockedUntil != null && !now.isBefore(current.blockedUntil)) {
                current.failedAttempts = 0;
                current.blockedUntil = null;
            }
            current.failedAttempts++;
            if (current.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                current.blockedUntil = now.plus(BLOCK_TIME);
            }
            return current;
        });
    }

    public void registerSuccessfulLogin(String username) {
        attempts.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private static final class AttemptState {
        private int failedAttempts;
        private Instant blockedUntil;
    }
}
