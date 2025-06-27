package lock.prac.Lock_Practice.global.oauth.converter;

import lock.prac.Lock_Practice.domain.user.entity.SocialType;
import lock.prac.Lock_Practice.domain.user.entity.User;


public class AuthConverter {
    public static User toUser(String email, String nickname, String profileImage, SocialType socialType, String socialId) {
        return User.builder()
                .email(email)
                .username(nickname)
                .profileImage(profileImage)
                .socialType(socialType)
                .socialId(socialId)
                .build();
    }
}