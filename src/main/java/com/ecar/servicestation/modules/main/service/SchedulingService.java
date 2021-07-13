package com.ecar.servicestation.modules.main.service;

import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Async
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class SchedulingService {

    private final ReservationRepository reservationRepository;

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

    // TODO: 예약 시간 알림
    // TODO: 종료 알림

}
