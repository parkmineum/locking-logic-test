package lock.prac.Lock_Practice.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lock.prac.Lock_Practice.domain.user.dto.UserResponseDTO;
import lock.prac.Lock_Practice.domain.user.entity.SocialType;
import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
import lock.prac.Lock_Practice.global.apiPayload.common.BaseResponse;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.jwt.JwtUtil;
import lock.prac.Lock_Practice.global.oauth.converter.AuthConverter;
import lock.prac.Lock_Practice.global.oauth.dto.response.GoogleResponse;
import lock.prac.Lock_Practice.global.oauth.dto.response.GoogleUserInfoResponse;
import lock.prac.Lock_Practice.global.oauth.dto.response.KakaoResponse;
import lock.prac.Lock_Practice.global.oauth.handler.AuthFailureHandler;
import lock.prac.Lock_Practice.global.oauth.util.CookieUtil;
import lock.prac.Lock_Practice.global.oauth.util.GoogleUtil;
import lock.prac.Lock_Practice.global.oauth.util.KakaoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;
    private final KakaoUtil kakaoUtil;
    private final GoogleUtil googleUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, CookieUtil cookieUtil, TokenService tokenService, KakaoUtil kakaoUtil, GoogleUtil googleUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.tokenService = tokenService;
        this.kakaoUtil = kakaoUtil;
        this.googleUtil = googleUtil;
    }

    @Transactional
    public User oAuthKaKaoLogin(String accessCode, String redirectUri, HttpServletResponse httpServletResponse) {
        try {
            KakaoResponse.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode, redirectUri);
            KakaoResponse.KakaoProfile kakaoProfile = kakaoUtil.requestProfile(oAuthToken);

            String socialId = String.valueOf(kakaoProfile.getId());
            String email = kakaoProfile.getKakao_account().getEmail();
            String nickname = kakaoProfile.getKakao_account().getProfile().getNickname();
            String profileImage = kakaoProfile.getKakao_account().getProfile().getProfile_image_url();


            User user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        // 이미 있는 유저, 소셜 정보만 업데이트
                        existingUser.setSocialType(SocialType.KAKAO);
                        existingUser.setSocialId(socialId);
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> createNewUser(email, nickname, profileImage, SocialType.KAKAO, socialId));

            // JWT 토큰 생성
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            long rtTtl = jwtUtil.getRemainingSeconds(refreshToken);
            log.info(" [Login] Redis에 리프레시 토큰 저장 시작. userId={}, ttl={}s", user.getId(), rtTtl);
            tokenService.storeRefreshToken(user.getId(), refreshToken, rtTtl);
            log.info(" [Login] Redis에 저장된 리프레시 토큰 = {}", tokenService.getStoredRefreshToken(user.getId()));

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setProfileImage(profileImage);
            userRepository.save(user);

            // 쿠키 저장
            cookieUtil.addCookie(httpServletResponse, "accessToken", accessToken, jwtUtil.getAccessTokenValidity());
            cookieUtil.addCookie(httpServletResponse, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidity());

            httpServletResponse.setHeader("Authorization", accessToken);

            return user;

        } catch (AuthFailureHandler e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 예외 발생", e);
            throw new AuthFailureHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public User oAuthGoogleLogin(String code, String redirectUri, HttpServletResponse response) {
        try {
            GoogleResponse token = googleUtil.requestToken(code, redirectUri);
            GoogleUserInfoResponse profile = googleUtil.requestProfile(token.getAccess_token());

            String socialId = profile.getId();
            String email = profile.getEmail();
            String nickname = profile.getNickname();
            String profileImage = profile.getProfileImage();

            User user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        // 이미 있는 유저, 소셜 정보만 업데이트
                        existingUser.setSocialType(SocialType.GOOGLE);
                        existingUser.setSocialId(socialId);
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> createNewUser(email, nickname, profileImage, SocialType.GOOGLE, socialId));


            // JWT 및 쿠키 처리 동일
            String accessToken = jwtUtil.createAccessToken(user.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

            long rtTtl = jwtUtil.getRemainingSeconds(refreshToken);
            tokenService.storeRefreshToken(user.getId(), refreshToken, rtTtl);

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setProfileImage(profileImage);
            userRepository.save(user);

            cookieUtil.addCookie(response, "accessToken", accessToken, jwtUtil.getAccessTokenValidity());
            cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidity());
            response.setHeader("Authorization", accessToken);

            return user;
        } catch (Exception e) {
            log.error("[Google OAuth2] 로그인 실패", e);
            throw new AuthFailureHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public User createNewUser(String email, String nickname, String profileImage, SocialType socialType, String socialId) {
        try {
            return userRepository.save(AuthConverter.toUser(email, nickname, profileImage, socialType, socialId));
        } catch (Exception e) {
            throw new AuthFailureHandler(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO.JoinInfoResultDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("[WARN] 사용자 ID {}에 해당하는 사용자를 찾을 수 없음", userId);
                    return new AuthFailureHandler(ErrorCode.USER_NOT_FOUND);});

        return new UserResponseDTO.JoinInfoResultDTO(user.getId(), user.getEmail(), user.getUsername(), user.getProfileImage());
    }

    @Transactional
    public BaseResponse<String> logout(Long userId, HttpServletRequest request, HttpServletResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthFailureHandler(ErrorCode.USER_NOT_FOUND));

        user.setRefreshToken(null);
        userRepository.save(user);

        tokenService.deleteAllRefreshTokens(userId);

        // 쿠키 방식일 경우도 처리
        cookieUtil.deleteCookie(response, "accessToken");
        cookieUtil.deleteCookie(response, "refreshToken");

        SecurityContextHolder.clearContext();

        return BaseResponse.onSuccess("로그아웃 성공");
    }

    @Transactional
    public BaseResponse<Void> reissueTokens(Long userId, HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            throw new AuthFailureHandler(ErrorCode.UNAUTHORIZED);
        }

        // validateToken → boolean 반환하도록 가정
        boolean isRefreshTokenValid = jwtUtil.isValid(refreshToken);
        if (!isRefreshTokenValid) {
            throw new AuthFailureHandler(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (!"refresh".equals(jwtUtil.extractCategory(refreshToken))) {
            throw new AuthFailureHandler(ErrorCode.INVALID_TOKEN_TYPE);
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthFailureHandler(ErrorCode.USER_NOT_FOUND));

        // Redis 에 저장된 토큰과 비교
        String saved = tokenService.getStoredRefreshToken(userId);
        if (saved == null || !saved.equals(refreshToken)) {
            tokenService.deleteAllRefreshTokens(userId);
            throw new AuthFailureHandler(ErrorCode.MULTI_ENV_LOGIN);
        }

        String newAccessToken = jwtUtil.createAccessToken(email);
        String newRefreshToken = refreshToken;

        // 토큰 만료 여부 판단 (jwtUtil에서 직접 체크하거나 내부 claims.exp 활용)
        if (jwtUtil.isExpired(refreshToken)) {
            newRefreshToken = jwtUtil.createRefreshToken(email);
            user.setRefreshToken(newRefreshToken);
        }
        long newRtTtl = jwtUtil.getRemainingSeconds(newRefreshToken);
        tokenService.storeRefreshToken(userId, newRefreshToken, newRtTtl);

        user.setAccessToken(newAccessToken);
        userRepository.save(user);

        cookieUtil.addCookie(response,
                "accessToken",
                newAccessToken,
                jwtUtil.getAccessTokenValidity());
        cookieUtil.addCookie(response,
                "refreshToken",
                newRefreshToken,
                jwtUtil.getRefreshTokenValidity());

        return BaseResponse.onSuccess(null);
    }

    private String resolveRefreshToken(HttpServletRequest request) {

        String token = CookieUtil.getCookieValue(request, "refreshToken");

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        return token;
    }
}
