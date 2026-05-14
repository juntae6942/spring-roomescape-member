package roomescape.reservation.application;

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
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;

@Transactional
@SpringBootTest
@Import(TestTimeConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("예약이 취소되면 다음 예약을 할 수 있다.")
    void canReservationAfterCancel() {
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
        ReservationInfo reservation = reservationService.addReservation(ReservationCreateCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(time.id())
                .themeId(theme.id())
                .build()
        );
        Assertions.assertThatThrownBy(() -> reservationService.addReservation(ReservationCreateCommand.builder()
                        .name("워니")
                        .date(LocalDate.now(clock))
                        .timeId(time.id())
                        .themeId(theme.id())
                .build()
        )).isInstanceOf(ReservationInUseException.class);
        reservationService.cancelReservation(reservation.id(), reservation.name());
        Assertions.assertThatCode(() -> reservationService.addReservation(ReservationCreateCommand.builder()
                .name("워니")
                .date(LocalDate.now(clock))
                .timeId(time.id())
                .themeId(theme.id())
                .build()
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("자신의 ID를 시간 변경 없이 그대로 수정해도 수정된다.")
    void canChangeTest() {
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
        ReservationInfo reservation = reservationService.addReservation(ReservationCreateCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(time.id())
                .themeId(theme.id())
                .build()
        );
        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .username(reservation.name())
                .date(reservation.date())
                .timeId(reservation.time().id())
                .themeId(reservation.theme().id()).build();

        Assertions.assertThatCode(() -> reservationService.changeReservation(reservation.id(), changeCommand))
                .doesNotThrowAnyException();
    }
}
