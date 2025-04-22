package lock.prac.Lock_Practice.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VoteSimulation {

    @Test
    void 동시_요청_중_하나만_투표에_성공해야_한다() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        AtomicInteger voteCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    ready.countDown(); // 스레드 준비 완료
                    start.await();     // 모든 스레드 동시에 시작

                    boolean lockAcquired = lock.tryLock();

                    if (lockAcquired) {
                        try {
                            voteCount.incrementAndGet();
                            successCount.incrementAndGet();
                        } finally {
                            lock.unlock();
                        }
                    }

                } catch (InterruptedException ignored) {
                }
            });
        }

        for (Thread thread : threads) thread.start();

        ready.await(); // 모든 스레드 준비될 때까지 대기
        start.countDown(); // 일제히 시작

        for (Thread thread : threads) thread.join();

        assertEquals(1, voteCount.get(), "투표는 단 1건만 발생해야 한다.");
        assertEquals(1, successCount.get(), "성공한 스레드는 단 1개여야 한다.");
    }

}
