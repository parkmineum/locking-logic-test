package lock.prac.Lock_Practice.domain.reservation.Service;

import lock.prac.Lock_Practice.domain.reservation.Entity.Seat;
import lock.prac.Lock_Practice.domain.reservation.repository.SeatReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lock.prac.Lock_Practice.domain.user.entity.User;
import lock.prac.Lock_Practice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;     // 분산 락을 위한 redissionClient
//    private final NamedLockRepository namedLockRepository;


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

    @Transactional
    public boolean reservationSeat(Long id){
        Seat seat = seatReservationRepository.findById(id).orElseThrow();
        if (seat.getQuantity() == 0) return false;

        seat.decreaseReserve();                              // 잔여 예약 즉시 감소
        seatReservationRepository.saveAndFlush(seat);
        return true;
    }

    @Transactional
    public Boolean distributedLock(Long id){
        String key = "LOCK";
        RLock rLock = redissonClient.getLock(key);
        boolean lockAcquired = false;

        try {
            boolean available = rLock.tryLock(5L, 3L, TimeUnit.SECONDS);               // 최대 5초 락 대기, 획득 후 3초 지나면 락 해제
            // 락 획득하지 못하면, false 반환
            // tryLock 은 InterruptedException 를 던질 수 있음


            if (!available) {
                return false;      // 락 획득에 실패한 경우
            }

            return reservationSeat(id);              // 락 획득에 성공한 경우만 좌석 예약 로직 실행
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();      // 인터럽트 발생 시 현재 스레드 상태 복구
            return false;

        } finally {
            if (lockAcquired && rLock.isHeldByCurrentThread()) {
                rLock.unlock();          // 락을 획득한 경우에만 해제
            }
        }
    }

    /**
     *  예약 가능한 좌석이 있을 경우에만 수량을 감소시키고 true 반환
     */
//    public boolean namedLockReservation(Long id) throws InterruptedException {
//        String key = "seat_lock_" + id;
//
//        // 트랜잭션 외부에서 락 획득
//        boolean lockAcquired = namedLockRepository.getLock(key, 3);
//        if (!lockAcquired) return false;
//
//        try {
//            return executeReservation(id);  // 트랜잭션 메서드 분리
//        } finally {
//            namedLockRepository.releaseLock(key);
//        }
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public boolean executeReservation(Long id) {
//        Seat seat = seatReservationRepository.findById(id).orElseThrow();
//        if (seat.getQuantity() == 0) return false;
//
//        seat.decreaseReserve();
//        seatReservationRepository.save(seat);
//        return true;
//    }
}
