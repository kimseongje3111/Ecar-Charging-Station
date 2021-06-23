package com.ecar.servicestation.modules.main.service;

import com.ecar.servicestation.modules.ecar.repository.ReservationTableRepository;
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

    private final ReservationTableRepository reservationRepository;

    private static final int OUTSTANDING_TIME_MINUTES = 15;

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60)
    @Transactional
    public void cancelOutstandingReservations() {
        long count = reservationRepository.updateStateFromStandByToCancelByOutstandingTime(OUTSTANDING_TIME_MINUTES);

        log.info("Scheduled task message : '{} reservations were cancelled'", count);
    }

    // TODO: 예약된 충전 시작 시간을 초과하고(15~20분?) 아직 충전을 시작하지 않은 예약 취소 처리
    // TODO: 종료 알림
}
