package lock.prac.Lock_Practice.global.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * SecurityContextHolder에 저장되는 인증 객체로 사용됨
 * JwtAuthenticationToken authentication = new JwtAuthenticationToken(email);
 * SecurityContextHolder.getContext().setAuthentication(authentication);
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;

    // JWT에서 추출한 Email로 인증 객체를 생성
    public JwtAuthenticationToken(Long userId) {
        super(null);
        this.userId = userId;
        setAuthenticated(true);     // 인증된 상태로 설정
    }

    @Override
    public Object getPrincipal() {
        return userId;         // 인증된 사용자의 이메일 반환
    }

    @Override
    public Object getCredentials() {
        return null;           // 비밀번호는 필요 없음
    }
}