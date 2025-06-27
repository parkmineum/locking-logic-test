package lock.prac.Lock_Practice.global.apiPayload.common;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)    // 실행 시점까지 어노테이션 정보를 유지한다는 뜻
@Parameter(hidden = true)              // @AuthUser로 주입되는 파라미터는 Swagger 문서에 노출되지 않도록 설정
public @interface AuthUser {
}
