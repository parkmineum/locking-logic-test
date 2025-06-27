package lock.prac.Lock_Practice.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lock.prac.Lock_Practice.domain.user.converter.UserConverter;
import lock.prac.Lock_Practice.domain.user.dto.UserResponseDTO;
import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.global.apiPayload.common.BaseResponse;
import lock.prac.Lock_Practice.global.apiPayload.response.Response;
import lock.prac.Lock_Practice.global.apiPayload.response.ResultCode;
import lock.prac.Lock_Practice.domain.user.service.AuthService;
//import lock.prac.Lock_Practice.global.oauth.util.KakaoUtil;
import lock.prac.Lock_Practice.global.config.properties.AppProperties;
import lock.prac.Lock_Practice.global.config.properties.GoogleRedirectProperties;
import lock.prac.Lock_Practice.global.config.properties.KakaoRedirectProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final KakaoRedirectProperties kakaoRedirectProperties;
    private final GoogleRedirectProperties googleRedirectProperties;
    private final AppProperties appProperties;

    @Operation(summary = "카카오 로그인", description = "인가코드와 redirectUri를 기반으로 로그인 처리")
    @GetMapping("/login/kakao")
    public BaseResponse<UserResponseDTO.JoinResultDTO> kakaoLogin(
            @RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {

        boolean isLocal = appProperties.isLocal();
        String redirectUri = isLocal ? kakaoRedirectProperties.getLocal() : kakaoRedirectProperties.getProd();

        User user = authService.oAuthKaKaoLogin(accessCode, redirectUri, httpServletResponse);
        log.info("api 실행 !!");
        return BaseResponse.onSuccess(UserConverter.toJoinResultDTO(user));
    }

    @GetMapping("/login/google")
    public BaseResponse<UserResponseDTO.JoinResultDTO> googleLogin(@RequestParam("code") String code, HttpServletResponse response) {

        boolean isLocal = appProperties.isLocal();
        String redirectUri = isLocal ? googleRedirectProperties.getLocal() : googleRedirectProperties.getProd();
        User user = authService.oAuthGoogleLogin(code, redirectUri, response);

        return BaseResponse.onSuccess(UserConverter.toJoinResultDTO(user));
    }

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/user")
    public Response<UserResponseDTO.JoinInfoResultDTO> getUserInfo(@AuthenticationPrincipal Long userId) {
        UserResponseDTO.JoinInfoResultDTO result = authService.getUserInfo(userId);
        return Response.of(ResultCode.USER_FETCH_OK, result);
    }

    @Operation(summary = "로그아웃", description = "사용자가 로그인한 계정의 리프레시 토큰을 삭제하고 쿠키를 제거합니다.")
    @PostMapping("/logout")
    public BaseResponse<String> logout(@AuthenticationPrincipal Long userId,  HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(userId, request, response);
    }

    @Operation(
            summary = "토큰 재발급", description = "리프레시 토큰을 사용해 새로운 액세스와 리프레시 토큰을 발급합니다")
    @PostMapping("/reissue")
    public BaseResponse<Void> reissue(@AuthenticationPrincipal Long userId, HttpServletRequest request, HttpServletResponse response) {
        authService.reissueTokens(userId, request, response);
        return BaseResponse.onSuccess(null);
    }

//    @Operation(summary = "회원 탈퇴", description = "회원 정보를 포함한 모든 관련 데이터를 삭제한 후, 계정을 완전히 제거합니다.")
//    @DeleteMapping("/delete")
//    public BaseResponse<String> deleteUser(HttpServletResponse response, boolean isLocal) {
//        return authService.deleteUser(response, isLocal);
//    }
}
