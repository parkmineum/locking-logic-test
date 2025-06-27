package lock.prac.Lock_Practice.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "oauth.kakao")
@Component
@Getter @Setter
public class KakaoRedirectProperties {
    private String clientId;
    private String local;
    private String prod;
}