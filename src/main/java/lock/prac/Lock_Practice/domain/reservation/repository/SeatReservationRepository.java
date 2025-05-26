package lock.prac.Lock_Practice.domain.reservation.repository;

import lock.prac.Lock_Practice.domain.reservation.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<Seat, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT s FROM Seat s where s.id = :id")
//    Optional<Seat> findByIdWithPessimisticLock(Long id);
}
