package lock.prac.Lock_Practice.domain.reservation.Entity;


import jakarta.persistence.*;
import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.global.apiPayload.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quantity;

    // 낙관적 락을 위한 version 필드 추가
//    @Version
//    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "seat")
    private List<User> userList = new ArrayList<>();

    public void decreaseReserve() {
        this.quantity--;
    }

    public void addUser(User user){
        userList.add(user);
        user.setSeat(this);
    }
}


