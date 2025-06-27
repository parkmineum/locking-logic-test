package lock.prac.Lock_Practice.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.oauth.handler.AuthFailureHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt_secret}")
    private String base64Secret;

    private Key secretKey;


    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
    }

    // email 기반의 JWT 생성
    public String createAccessToken(String email) {
        try {
            return generateToken(email, accessTokenValidity,"access");
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.JWT_GENERATION_FAILED);
        }
    }

    public String createRefreshToken(String email) {
        try {
            return generateToken(email, refreshTokenValidity, "refresh");
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.JWT_GENERATION_FAILED);
        }
    }

    private String generateToken(String email, long validity, String category) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setSubject(email)
                .claim("category", category)      // "access" 또는 "refresh"
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new AuthFailureHandler(ErrorCode.JWT_EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new AuthFailureHandler(ErrorCode.JWT_INVALID_TOKEN);
        }
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // JWT 발급 시 필요한 시크릿 키를 Key 객체로 변환 (서명 검증)
    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.JWT_GENERATION_FAILED);
        }
    }

    // JWT 의 payload(Claims 객체) 추출
    private Claims parseClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public String extractCategory(String token) {
        return parseClaims(token).get("category", String.class);
    }

    // 토큰 만료 시간
    public long getRemainingSeconds(String token) {
        Date exp = parseClaims(token).getExpiration();
        return (exp.getTime() - System.currentTimeMillis()) / 1000;
    }

    public int getAccessTokenValidity() {
        return (int) (accessTokenValidity / 1000);   // JWT 유효 시간 형변환
    }

    public int getRefreshTokenValidity() {
        return (int) (refreshTokenValidity / 1000);  // JWT 유효 시간 형변환
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("userId", Long.class); // 또는 claims.getSubject() → Long.parseLong
        } catch (Exception e) {
            log.warn("[JWT] userId 추출 실패", e);
            return null;
        }
    }

    public boolean isExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean isValid(String token) {
        try {
            validateToken(token); // 내부적으로 예외 던짐
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
