package lock.prac.Lock_Practice.global.oauth.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleResponse {
    private String access_token;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String token_type;
    private String id_token;
}
