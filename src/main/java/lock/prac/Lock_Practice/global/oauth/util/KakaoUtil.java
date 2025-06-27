package lock.prac.Lock_Practice.global.oauth.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.config.properties.KakaoRedirectProperties;
import lock.prac.Lock_Practice.global.oauth.dto.response.KakaoResponse;
import lock.prac.Lock_Practice.global.oauth.handler.AuthFailureHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Component
@Slf4j
public class KakaoUtil {

    private final ObjectMapper objectMapper;
    private final KakaoRedirectProperties kakaoRedirectProperties;

    private final Set<String> allowedRedirectUris = Set.of(
            "http://localhost:8080/api/auth/login/kakao",
            "http://localhost:8080/login/oauth2/code/kakao", "http://localhost:3000/login/oauth2/code/kakao"
    );

    public KakaoUtil(ObjectMapper objectMapper,  KakaoRedirectProperties kakaoRedirectProperties) {
        this.objectMapper = objectMapper;
        this.kakaoRedirectProperties = kakaoRedirectProperties;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // JSON 필드가 존재하지 않을 경우 무시하고 파싱
    }

    public KakaoResponse.OAuthToken requestToken(String accessCode, String redirectUri) {

        redirectUri = redirectUri.trim();

        if (!allowedRedirectUris.contains(redirectUri)) {
            log.error("[🚨ERROR🚨] 허용되지 않은 redirect_uri 요청: {}", redirectUri);
            throw new AuthFailureHandler(ErrorCode.KAKAO_INVALID_GRANT);
        }

        // 요청 헤더 및 파라미터 구성
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRedirectProperties.getClientId());
        params.add("redirect_uri", redirectUri);
        params.add("code", accessCode);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        log.info("[DEBUG] 카카오 토큰 요청 시작 - redirect_uri: {}, client_id: {}, accessCode: {}", redirectUri, kakaoRedirectProperties.getClientId(), accessCode);

        try {
            // 카카오 토큰 엔드포인트에 POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );

            log.info("[RESPONSE] 카카오 API 응답: {}", response.getBody());

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new AuthFailureHandler(ErrorCode.KAKAO_AUTH_FAILED);
            }

            // 정상 응답일 경우, JSON 응답을 KakaoDTO.OAuthToken 객체로 역직렬화하여 반환
            return objectMapper.readValue(response.getBody(), KakaoResponse.OAuthToken.class);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("[🚨ERROR🚨] 유효하지 않은 카카오 인증 코드 (401 Unauthorized)");
            throw new AuthFailureHandler(ErrorCode.KAKAO_INVALID_GRANT);
        } catch (JsonProcessingException e) {
            log.error("[🚨ERROR🚨] 카카오 응답 JSON 파싱 오류: {}", e.getMessage());
            throw new AuthFailureHandler(ErrorCode.KAKAO_JSON_PARSE_ERROR);
        } catch (Exception e) {
            log.error("[🚨ERROR🚨] 카카오 API 호출 중 오류 발생: {}", e.getMessage());
            throw new AuthFailureHandler(ErrorCode.KAKAO_API_ERROR);
        }
    }

    public KakaoResponse.KakaoProfile requestProfile(KakaoResponse.OAuthToken oAuthToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            log.info("[DEBUG] 카카오 프로필 응답: {}", response.getBody());

            return objectMapper.readValue(response.getBody(), KakaoResponse.KakaoProfile.class);

        } catch (JsonProcessingException e) {
            log.error("[🚨ERROR🚨] 카카오 프로필 파싱 오류: {}", e.getMessage());
            throw new AuthFailureHandler(ErrorCode.KAKAO_JSON_PARSE_ERROR);
        } catch (Exception e) {
            log.error("[🚨ERROR🚨] 카카오 프로필 요청 중 오류 발생: {}", e.getMessage());
            throw new AuthFailureHandler(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
