package lock.prac.Lock_Practice.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "oauth.google")
@Component
@Getter
@Setter
public class GoogleRedirectProperties {
    private String clientId;
    private String clientSecret;
    private String local;
    private String prod;
}
