package lock.prac.Lock_Practice.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redis;
    private static final String REFRESH_KEY_PREFIX = "refreshToken:";

    // 저장
    public void storeRefreshToken(Long userId, String token, long ttlSeconds) {
        String key = REFRESH_KEY_PREFIX + userId;
        redis.opsForValue().set(key, token, Duration.ofSeconds(ttlSeconds));
    }

    // 조회
    public String getStoredRefreshToken(Long userId) {
        String key = REFRESH_KEY_PREFIX + userId;
        return redis.opsForValue().get(key);
    }

    // 삭제
    public void deleteAllRefreshTokens(Long userId) {
        String key = REFRESH_KEY_PREFIX + userId;
        redis.delete(key);
    }
}