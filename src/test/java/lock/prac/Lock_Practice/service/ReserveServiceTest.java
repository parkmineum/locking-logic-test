package lock.prac.Lock_Practice.service;

import lock.prac.Lock_Practice.domain.Entity.Seat;
import lock.prac.Lock_Practice.domain.Service.SeatReservationService;
import lock.prac.Lock_Practice.domain.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

@SpringBootTest
public class ReserveServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private SeatReservationService seatReservationService;

    @Test
    void ReservatiionTest() throws InterruptedException {

        // given
        final Long TOTAL_SEATS = 30L;
        final int TOTAL_USERS = 50;
        final Long SEAT_ID = seatReservationService.create(TOTAL_SEATS);      // 테스트할 좌석 id

        CountDownLatch countDownLatch = new CountDownLatch(TOTAL_USERS);        // 동시성 제어를 위한 도구

        // when
        List<SeatReservateWorker> workers = Stream.generate(() -> new SeatReservateWorker(userService, seatReservationService, SEAT_ID, countDownLatch))
                .limit(TOTAL_USERS)
                .toList();

        workers.forEach(worker -> new Thread(worker).start());
        countDownLatch.await();       // 모든 작업이 완료될 때까지 대기

        // then
        Seat updateSeat = seatReservationService.findReservationById(SEAT_ID);
        assertThat(updateSeat.getQuantity()).isEqualTo(0);
    }

    private class SeatReservateWorker implements Runnable {
        private final UserService userService;
        private final SeatReservationService seatReservationService;
        private final Long reserveId;
        private final CountDownLatch countDownLatch;


        public SeatReservateWorker(UserService userService, SeatReservationService seatReservationService, Long reserveId, CountDownLatch countDownLatch) {
            this.userService = userService;
            this.seatReservationService = seatReservationService;
            this.reserveId = reserveId;
            this.countDownLatch = countDownLatch;
        }


        @Override
        public void run() {
            Long memberId = userService.join("Park");
            boolean result = seatReservationService.distributedLock(reserveId);

            if (result) {
                System.out.println("예약에 성공하였습니다.");
                seatReservationService.addUser(reserveId, memberId);
            }
            else {
                System.out.println("예약에 실패하였습니다.");
            }
            countDownLatch.countDown();
        }
    }
}

