package lock.prac.Lock_Practice.domain.user.repository;

import lock.prac.Lock_Practice.domain.user.entity.SocialType;
import lock.prac.Lock_Practice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
