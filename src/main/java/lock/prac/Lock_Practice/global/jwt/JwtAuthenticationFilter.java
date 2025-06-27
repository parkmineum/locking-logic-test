package lock.prac.Lock_Practice.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
import lock.prac.Lock_Practice.global.apiPayload.exception.BusinessException;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.oauth.handler.AuthFailureHandler;
import lock.prac.Lock_Practice.global.oauth.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final List<String> EXCLUDED_URLS = List.of(
            "/login/oauth2/code/kakao", "/api/auth/login/kakao", "/api/auth/login/google", "/swagger-ui", "/v3/api-docs", "/login", "/login/success"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 인증이 필요 없는 요청이면 필터를 통과시킴
        if (isExcluded(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT 토큰 추출
        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 현재 로그인한 사용자 정보 SecurityContext에 인증 객체 설정
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(user.getId());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 인증이 성공했든 아니든 다음 필터로 흐름을 넘김
            filterChain.doFilter(request, response);
        }else {
            log.warn("[401]JWT 필터 인증 실패");
            throw new AuthFailureHandler(ErrorCode.JWT_INVALID_TOKEN);
        }
    }

    private boolean isExcluded(String requestURI) {
        return EXCLUDED_URLS.stream().anyMatch(requestURI::startsWith);
    }


    // 앱과 웹 브라우저 요청에 모두 대응이 가능하도록 설계
    private String extractToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 헤더에 없으면 쿠키에서 accessToken 가져옴
        return CookieUtil.getCookieValue(request, "accessToken");
    }

//    private String extractToken(HttpServletRequest request) {
//
//        String cookieToken = CookieUtil.getCookieValue(request, "accessToken");
//
//        if (cookieToken != null) {
//            log.info("[JWT] 쿠키에서 accessToken 추출 성공");
//        } else {
//            log.warn("[JWT] 토큰을 찾을 수 없음 (쿠키 없음)");
//        }
//
//        return cookieToken;
//    }
}
