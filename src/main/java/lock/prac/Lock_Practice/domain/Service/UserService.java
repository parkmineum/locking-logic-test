package lock.prac.Lock_Practice.domain.Service;

import lock.prac.Lock_Practice.domain.Entity.User;
import lock.prac.Lock_Practice.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    public Long join(String name) {
        User user = new User(name);
        user.setName(name);
        userRepository.save(user);
        return user.getId(); // PK 반환
    }
}
