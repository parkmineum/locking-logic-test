package lock.prac.Lock_Practice.global.oauth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lock.prac.Lock_Practice.global.config.properties.AppProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    private final AppProperties appProperties;

    public CookieUtil(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {

        boolean isLocal = appProperties.isLocal();

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);     // HTTPS 에서만 쿠키 허용
        cookie.setSecure(!isLocal);
        cookie.setMaxAge(maxAge);

        if (!isLocal) {
            cookie.setAttribute("SameSite", "None");    // 운영 환경에서는 크로스사이트 요청 가능하도록 설정

        } else {
            cookie.setAttribute("SameSite", "Lax");    // 로컬에서는 기본값 설정
        }

        response.addCookie(cookie);
    }

    // 쿠키를 직접 삭제하는 것이 아닌, 빈 쿠키를 덮어써서 삭제한다.
    public void deleteCookie(HttpServletResponse response, String name) {

        boolean isLocal = appProperties.isLocal();

        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(!isLocal);

        if (!isLocal) {
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setAttribute("SameSite", "Lax");
        }

        response.addCookie(cookie);
    }


    // 특정 이름을 가진 쿠키의 값 추출 (주로 accessToken)
    public static String getCookieValue(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(name))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse(null);
    }
}