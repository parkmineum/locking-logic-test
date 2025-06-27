package lock.prac.Lock_Practice.global.oauth.handler;

import lock.prac.Lock_Practice.global.apiPayload.common.BaseResponse;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lombok.Getter;

@Getter
public class AuthFailureHandler extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthFailureHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseResponse<String> toResponse() {
        return new BaseResponse<>(false, errorCode.getCode(), errorCode.getMessage());
    }
}