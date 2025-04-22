package lock.prac.Lock_Practice.domain.repository;

import lock.prac.Lock_Practice.domain.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
