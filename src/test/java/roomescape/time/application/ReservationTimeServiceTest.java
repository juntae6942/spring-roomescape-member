package roomescape.time.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.presentation.dto.AvailableReservationTimeRequest;
import roomescape.time.presentation.dto.ReservationTimeRequest;

@Transactional
@SpringBootTest
@Import(TestTimeConfig.class)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService timeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("오늘 해당 테마의 예약이 1개 있고, 총 시간이 3개 있으면, 남은 시간은 2개이다.")
    void timeAvailableTest() {
        ThemeRequest theme = ThemeRequest.builder()
                .name("미드나잇")
                .thumbnailImageUrl("https://example.com/theme.png")
                .description("추리 테마")
                .durationTime(LocalTime.now(clock))
                .build();
        ThemeInfo savedTheme = themeService.addTheme(theme.toCommand());
        ReservationTimeInfo time = timeService.addReservationTime(new ReservationTimeRequest(LocalTime.now(clock)).toCommand());
        timeService.addReservationTime(new ReservationTimeRequest(LocalTime.now(clock).plusHours(1)).toCommand());
        timeService.addReservationTime(new ReservationTimeRequest(LocalTime.now(clock).plusHours(2)).toCommand());
        reservationService.addReservation(new ReservationCreateCommand("포비", LocalDate.now(clock), time.id(), savedTheme.id()));
        AvailableReservationTimeRequest availableReservationTimeRequest = new AvailableReservationTimeRequest(
                savedTheme.id(), LocalDate.now(clock));
        Assertions.assertThat(timeService.getAvailableReservationTime(availableReservationTimeRequest.toCommand())
                .times())
                .hasSize(2);
    }

    @Test
    @DisplayName("오늘 해당 테마의 예약이 1개 있고, 취소가 1개 있을 때, 총 시간이 3개라면, 남은 시간은 2개다.")
    void timeAvailableTestWithCancelReturn2() {
        ThemeRequest theme = ThemeRequest.builder()
                .name("미드나잇")
                .thumbnailImageUrl("https://example.com/theme.png")
                .description("추리 테마")
                .durationTime(LocalTime.now(clock))
                .build();
        ThemeInfo savedTheme = themeService.addTheme(theme.toCommand());
        ReservationTimeInfo time1 = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock)).toCommand());
        ReservationTimeInfo time2 = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock).plusHours(1)).toCommand());
        timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock).plusHours(2)).toCommand());

        ReservationInfo reservation = reservationService.addReservation(
                new ReservationCreateCommand("포비", LocalDate.now(clock), time1.id(), savedTheme.id()));
        reservationService.addReservation(
                new ReservationCreateCommand("리사", LocalDate.now(clock), time2.id(), savedTheme.id()));
        reservationService.cancelReservation(reservation.id(), reservation.name());
        AvailableReservationTimeRequest availableReservationTimeRequest = new AvailableReservationTimeRequest(
                savedTheme.id(), LocalDate.now(clock));
        Assertions.assertThat(
                timeService.getAvailableReservationTime(availableReservationTimeRequest.toCommand())
                .times())
                .hasSize(2);
    }

    @Test
    @DisplayName("오늘 해당 테마의 예약이 2개 있고, 취소가 1개 있을 때, 총 시간이 3개면, 남은 시간은 1개다.")
    void timeAvailableTestWithCancelReturn1() {
        ThemeRequest theme = ThemeRequest.builder()
                .name("미드나잇")
                .thumbnailImageUrl("https://example.com/theme.png")
                .description("추리 테마")
                .durationTime(LocalTime.now(clock))
                .build();
        ThemeInfo savedTheme = themeService.addTheme(theme.toCommand());
        ReservationTimeInfo time1 = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock)).toCommand());
        ReservationTimeInfo time2 = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock).plusHours(1)).toCommand());
        ReservationTimeInfo time3 = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now(clock).plusHours(2)).toCommand());

        ReservationInfo reservation1 = reservationService.addReservation(
                new ReservationCreateCommand("포비", LocalDate.now(clock), time1.id(), savedTheme.id()));
        ReservationInfo reservation2 = reservationService.addReservation(
                new ReservationCreateCommand("리사", LocalDate.now(clock), time2.id(), savedTheme.id()));
        ReservationInfo reservation3 = reservationService.addReservation(
                new ReservationCreateCommand("워니", LocalDate.now(clock), time3.id(), savedTheme.id()));

        reservationService.cancelReservation(reservation1.id(), reservation1.name());
        AvailableReservationTimeRequest availableReservationTimeRequest = new AvailableReservationTimeRequest(
                savedTheme.id(), LocalDate.now(clock));
        Assertions.assertThat(
                        timeService.getAvailableReservationTime(availableReservationTimeRequest.toCommand())
                                .times())
                .hasSize(1);
    }
}
