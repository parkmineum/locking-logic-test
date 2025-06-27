package lock.prac.Lock_Practice.global.apiPayload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    OK(HttpStatus.OK, "COMMON200", "성공입니다."),

    // Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "찾을 수 없는 요청입니다."),


    // User Error
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER_401", "로그인 정보가 없습니다."),
    USER_NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, "USER_401", "로그인 하지 않았습니다."),
    USER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "USER_403", "권한이 없습니다."),

    // JWT 관련 에러
    JWT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_500", "JWT 토큰 생성 중 오류가 발생했습니다."),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_401", "유효하지 않은 JWT 토큰입니다."),
    JWT_EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "JWT_402", "만료된 JWT 토큰입니다."),
    INVALID_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "JWT_402", "잘못된 유형의 토큰입니다."),
    MULTI_ENV_LOGIN(HttpStatus.UNAUTHORIZED, "JWT_403", "다른 환경에서 로그인되었습니다. 다시 로그인해 주세요."),


    // 카카오 OAuth2 관련 에러
    KAKAO_AUTH_FAILED(HttpStatus.BAD_REQUEST, "KAKAO_400", "카카오 인증 요청 중 오류가 발생했습니다."),
    KAKAO_INVALID_GRANT(HttpStatus.UNAUTHORIZED, "KAKAO_401", "유효하지 않은 카카오 인증 코드입니다."),
    KAKAO_API_ERROR(HttpStatus.BAD_REQUEST, "KAKAO_402", "카카오 API 호출 중 문제가 발생했습니다."),
    KAKAO_JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_500", "카카오 응답 JSON 파싱 중 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .status(status)
                .code(code)
                .message(message)
                .build()
                ;
    }
}
