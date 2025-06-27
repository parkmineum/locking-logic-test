package lock.prac.Lock_Practice.global.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

public class KakaoResponse {

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OAuthToken {     // 카카오 서버 응답 JSON -> DTO 객체로 변환
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProfile {
        private Long id;
        private String connected_at;
        private Properties properties;
        private KakaoAccount kakao_account;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Properties {
            private String nickname;
        }

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean has_email;
            private Boolean profile_nickname_needs_agreement;
            private Boolean email_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public class Profile {
                private String nickname;
                private Boolean is_default_nickname;
                private String profile_image_url;
            }
        }
    }
}
