package lock.prac.Lock_Practice.global.apiPayload.exception;

import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import org.springframework.http.HttpStatus;

public class GlobalException extends RuntimeException {

    private final ErrorCode code;

    public GlobalException(ErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return this.code.getStatus();
    }

    public ErrorCode getReason() {
        return this.code;
    }
}

