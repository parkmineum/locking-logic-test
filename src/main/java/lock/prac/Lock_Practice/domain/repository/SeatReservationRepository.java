package lock.prac.Lock_Practice.domain.repository;

import jakarta.persistence.LockModeType;
import lock.prac.Lock_Practice.domain.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SeatReservationRepository extends JpaRepository<Seat, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT s FROM Seat s where s.id = :id")
//    Optional<Seat> findByIdWithPessimisticLock(Long id);
}
