package lock.prac.Lock_Practice.domain.Service;

import lock.prac.Lock_Practice.domain.Entity.Seat;
import lock.prac.Lock_Practice.domain.Entity.User;
import lock.prac.Lock_Practice.domain.repository.NamedLockRepository;
import lock.prac.Lock_Practice.domain.repository.SeatReservationRepository;
import lock.prac.Lock_Practice.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;


@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserRepository userRepository;
//    private final RedissonClient redissonClient;     // 분산 락을 위한 redissionClient
    private final NamedLockRepository namedLockRepository;


    @Transactional
    public Long create(Long quantity) {
        Seat seat = Seat.builder().quantity(quantity).build();
        seatReservationRepository.save(seat);
        return seat.getId();
    }

    @Transactional
    public void addUser(Long reserveId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Seat seat = findReservationById(reserveId);
        seat.addUser(user);
        seatReservationRepository.save(seat);
    }

    public Seat findReservationById(Long id) {
        return seatReservationRepository.findById(id).orElseThrow();
    }

    /**
     *  예약 가능한 좌석이 있을 경우에만 수량을 감소시키고 true 반환
     */
    public boolean namedLockReservation(Long id) throws InterruptedException {
        String key = "seat_lock_" + id;

        // 트랜잭션 외부에서 락 획득
        boolean lockAcquired = namedLockRepository.getLock(key, 3);
        if (!lockAcquired) return false;

        try {
            return executeReservation(id);  // 트랜잭션 메서드 분리
        } finally {
            namedLockRepository.releaseLock(key);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean executeReservation(Long id) {
        Seat seat = seatReservationRepository.findById(id).orElseThrow();
        if (seat.getQuantity() == 0) return false;

        seat.decreaseReserve();
        seatReservationRepository.save(seat);
        return true;
    }
}
