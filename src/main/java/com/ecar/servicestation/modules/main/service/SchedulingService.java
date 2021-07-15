package com.ecar.servicestation.modules.main.service;

import com.ecar.servicestation.infra.notification.service.PushMessageService;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.DeviceToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Async
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class SchedulingService {

    private final ReservationRepository reservationRepository;
    private final PushMessageService pushMessageService;

    private static final int UNPAID_TIME_MINUTES = 15;
    private static final int NO_SHOW_TIME_MINUTES = 30;

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60)
    @Transactional
    public void cancelOutstandingReservations() {
        long count = reservationRepository.updateStateToCancelByUnpaidTimeOver(UNPAID_TIME_MINUTES);

        if (count > 0) {
            log.info("Scheduled task message : '{} reservations were cancelled due to unpaid'", count);
        }
    }

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60)
    @Transactional
    public void cancelNoShowReservations() {
        long count = reservationRepository.updateStateToCancelByNoShowTimeOver(NO_SHOW_TIME_MINUTES);

        if (count > 0) {
            log.info("Scheduled task message : '{} reservations were cancelled due to no-show'", count);
        }
    }

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60)
    @Transactional
    public void notificationReservationStart() {
        List<ReservationTable> notificationTargets =
                reservationRepository.findAllWithAccountByPaymentStateAndCanNotificationOfReservationStart();

        if (notificationTargets.size() > 0) {
            LocalDateTime now = LocalDateTime.now();

            notificationTargets =
                    notificationTargets.stream()
                            .filter(reservation -> {
                                LocalDateTime chargeStartDateTime = reservation.getChargeStartDateTime();
                                int minutesBeforeReservationStart = reservation.getAccount().getNotificationMinutesBeforeReservationStart();

                                return chargeStartDateTime.minusMinutes(minutesBeforeReservationStart).equals(now)
                                        || chargeStartDateTime.minusMinutes(minutesBeforeReservationStart).isBefore(now);
                            })
                            .collect(Collectors.toList());

            notificationTargets.forEach(reservation -> {
                DeviceToken deviceToken = reservation.getAccount().getDeviceToken();
                String title = "충전 예약 시작 사전 알림";
                String body = "곧 시작하는 충전 예약이 있습니다. 앱에서 시간을 확인해 주세요.";

                pushMessageService.sendPushMessageTo(deviceToken.getDeviceUniqueToken(), title, body);
                reservation.setSentNotificationOfReservationStart(true);
            });
        }
    }

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60)
    @Transactional
    public void notificationChargingEnd() {
        List<ReservationTable> notificationTargets =
                reservationRepository.findAllWithAccountByChargingStateAndCanNotificationOfChargingEnd();

        if (notificationTargets.size() > 0) {
            LocalDateTime now = LocalDateTime.now();

            notificationTargets =
                    notificationTargets.stream()
                            .filter(reservation -> {
                                LocalDateTime chargeEndDateTime = reservation.getChargeEndDateTime();
                                Integer minutesBeforeChargingEnd = reservation.getAccount().getNotificationMinutesBeforeChargingEnd();

                                return chargeEndDateTime.minusMinutes(minutesBeforeChargingEnd).equals(now)
                                        || chargeEndDateTime.minusMinutes(minutesBeforeChargingEnd).isBefore(now);
                            })
                            .collect(Collectors.toList());

            notificationTargets.forEach(reservation -> {
                DeviceToken deviceToken = reservation.getAccount().getDeviceToken();
                String title = "충전 종료 예정 알림";
                String body = "충전이 곧 종료될 예정입니다. 시간 내에 차량을 회수해 주세요.";

                pushMessageService.sendPushMessageTo(deviceToken.getDeviceUniqueToken(), title, body);
                reservation.setSentNotificationOfChargingEnd(true);
            });
        }
    }

}
