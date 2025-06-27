//package lock.prac.Lock_Practice.global.oauth.handler;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lock.prac.Lock_Practice.domain.user.entity.User;
//import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
//import lock.prac.Lock_Practice.global.jwt.JwtUtil;
//import lock.prac.Lock_Practice.domain.user.service.AuthService;
//import lock.prac.Lock_Practice.global.oauth.util.CookieUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Map;
//
///**
// *  OAuth 인증 성공 후 실행하여 JWT 발급 및 쿠키 저장
// */
//@Component
//@Slf4j
//public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtUtil jwtUtil;
//    private final UserRepository userRepository;
//    private final AuthService authService;
//    private final CookieUtil cookieUtil;
//
//    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository, AuthService authService, CookieUtil cookieUtil) {
//        this.jwtUtil = jwtUtil;
//        this.userRepository = userRepository;
//        this.authService = authService;
//        this.cookieUtil = cookieUtil;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        String state = request.getParameter("state");
//        boolean isLocal = "local".equals(state);
//
//        // Spring 이 인증된 사용자 정보 => OAuth2User 자동 주입
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//
//        // Kakao 사용자 정보 구조 파싱
//        Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
//        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
//
//        String email = (String) kakaoAccount.get("email");
//        String nickname = (String) profile.get("nickname");
//        String profileImage = (String) profile.get("profile_image_url");
//
//        User user = userRepository.findByEmail(email)
//                .orElseGet(() -> authService.createNewUser(email, nickname, profileImage));
//
//        // JWT 생성
//        String accessToken = jwtUtil.createAccessToken(String.valueOf(user.getEmail()));
//        String refreshToken = jwtUtil.createRefreshToken(String.valueOf(user.getEmail()));
//
//        user.setAccessToken(accessToken);
//        user.setRefreshToken(refreshToken);
//        user.setProfileImage(profileImage);
//        userRepository.save(user);
//
//        // 쿠키 저장
//        cookieUtil.addCookie(response, "accessToken", accessToken, jwtUtil.getAccessTokenValidity());
//        cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidity());
//        response.setHeader("Authorization", accessToken);
//
//        log.info("[OAuth2] 로그인 성공, userId: {}", user.getId());
//
//        String redirectUrl = isLocal ? "http://localhost:3000/login/success" : "/login/success";   // 프론트 배포 주소
//        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
//    }
//}