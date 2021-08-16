package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.user.domain.Account;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    ReservationTable findReservationWithChargerAndCarById(long reservationId);

    ReservationTable findReservationWithChargerAndCarByReserveTitle(String reserveTitle);

    ReservationTable findReservationWithChargerAndAccountByReserveTitle(String reserveTitle);

    List<ReservationTable> findAllWithChargerAndCarByAccountAndNotCancel(long accountId);

    List<ReservationTable> findAllWithChargerAndCarByAccountAndComplete(long accountId);

    List<ReservationTable> findAllByChargerAndStateAndBetweenDateTime(long chargerId, ReservationState state, LocalDateTime start, LocalDateTime end);

    List<ReservationTable> findAllByChargerAndReservedStateAndStartBetweenDateTime(long chargerId, LocalDateTime start, LocalDateTime end);

    List<ReservationTable> findAllWithAccountByPaymentStateAndCanNotificationOfReservationStart();

    List<ReservationTable> findAllWithAccountByChargingStateAndCanNotificationOfChargingEnd();

    long updateStateToCancelByUnpaidTimeOver(int unpaidTimeMinutes);

    long updateStateToCancelByNoShowTimeOver(int noShowTimeMinutes);


}
