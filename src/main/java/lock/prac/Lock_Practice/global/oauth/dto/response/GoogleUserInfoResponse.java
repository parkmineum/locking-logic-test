package lock.prac.Lock_Practice.global.oauth.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfoResponse {
    private String id;
    private String email;
    private String nickname;
    private String profileImage;
}
