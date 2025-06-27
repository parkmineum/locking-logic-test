package lock.prac.Lock_Practice.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lock.prac.Lock_Practice.global.apiPayload.common.AuthUser;
import lock.prac.Lock_Practice.global.apiPayload.exception.BusinessException;
import lock.prac.Lock_Practice.global.apiPayload.response.ErrorCode;
import lock.prac.Lock_Practice.global.oauth.handler.AuthFailureHandler;
import lock.prac.Lock_Practice.global.oauth.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    // ArgumentResolver 가 어떤 파라미터를 처리할지 여부를 결정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AuthUser.class) != null
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {


        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String token = CookieUtil.getCookieValue(request, "accessToken");
        log.info("[AuthUser] Extracted token: {}", token);


        if (token == null) {
            throw new AuthFailureHandler(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("[AuthUser] Extracted userId: {}", userId);

        return userId;
    }
}