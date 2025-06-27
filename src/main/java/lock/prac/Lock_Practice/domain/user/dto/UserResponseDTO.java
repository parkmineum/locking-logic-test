package lock.prac.Lock_Practice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class UserResponseDTO {

    @Getter
    @AllArgsConstructor
    public static class JoinResultDTO {     // 가입 후 반환할 정보
        private String email;
        private String nickname;
        private String profileImage;

        private String accessToken;
        private String refreshToken;
    }
    @Getter
    @AllArgsConstructor
    public static class JoinInfoResultDTO {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;
    }
}
