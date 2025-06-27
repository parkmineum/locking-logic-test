package lock.prac.Lock_Practice.global.oauth.util;

import lock.prac.Lock_Practice.global.config.properties.GoogleRedirectProperties;
import lock.prac.Lock_Practice.global.oauth.dto.response.GoogleResponse;
import lock.prac.Lock_Practice.global.oauth.dto.response.GoogleUserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GoogleUtil {

    private final GoogleRedirectProperties googleRedirectProperties;

    public GoogleUtil(GoogleRedirectProperties googleRedirectProperties) {
        this.googleRedirectProperties = googleRedirectProperties;
    }

    public GoogleResponse requestToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleRedirectProperties.getClientId());
        params.add("client_secret", googleRedirectProperties.getClientSecret());
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<GoogleResponse> response = new RestTemplate()
                .postForEntity("https://oauth2.googleapis.com/token", request, GoogleResponse.class);

        return response.getBody();
    }

    public GoogleUserInfoResponse requestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfoResponse> response = new RestTemplate()
                .exchange("https://www.googleapis.com/oauth2/v2/userinfo", HttpMethod.GET, entity, GoogleUserInfoResponse.class);

        return response.getBody();
    }
}

