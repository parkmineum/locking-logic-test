package lock.prac.Lock_Practice.domain.user.entity;

import jakarta.persistence.*;
import lock.prac.Lock_Practice.domain.reservation.Entity.Seat;
import lock.prac.Lock_Practice.global.apiPayload.common.BaseEntity;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    public User(String name, String username, String profileImage) {
        this.username = name;
    }
}
