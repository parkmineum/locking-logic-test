package lock.prac.Lock_Practice.domain.reservation.Service;

import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

}
