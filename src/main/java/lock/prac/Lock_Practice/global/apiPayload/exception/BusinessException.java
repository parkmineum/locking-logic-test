package lock.prac.Lock_Practice.global.apiPayload.exception;

import lock.prac.Lock_Practice.global.apiPayload.response.BaseCode;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.apiPayload.response.ReasonDTO;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ReasonDTO getReason() {
        return this.errorCode.getReason();
    }

    public BaseCode getCode() {
        return errorCode;
    }
}