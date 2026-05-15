package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;

@SpringBootTest
public class ReservationConcurrencyTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("동시에 100명의 사용자가 같은 테마, 같은 시간 예약을 요청하면 1명만 예약된다.")
    void concurrencyReserveTest() throws InterruptedException {
        ReservationTimeInfo time = reservationTimeService.addReservationTime(ReservationTimeCommand.builder()
                .startAt(LocalTime.now(clock))
                .build()
        );
        ThemeInfo theme = themeService.addTheme(ThemeCommand.builder()
                .name("포비")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .description("포비가 나와요")
                .build()
        );
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    ReservationCreateCommand createCommand = ReservationCreateCommand.builder()
                            .name("포비")
                            .date(LocalDate.now(clock))
                            .timeId(time.id())
                            .themeId(theme.id())
                            .build();
                    reservationService.addReservation(createCommand);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Assertions.assertThat(reservationService.getReservations().size()).isOne();
    }
}
