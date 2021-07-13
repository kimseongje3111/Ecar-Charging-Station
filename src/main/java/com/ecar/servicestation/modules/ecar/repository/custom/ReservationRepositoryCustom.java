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

    List<ReservationTable> findAllWithChargerAndCarByAccountAndState(long accountId, ReservationState state);

    List<ReservationTable> findAllByChargerAndStateAndBetweenDateTime(long chargerId, ReservationState state, LocalDateTime start, LocalDateTime end);

    long updateStateToCancelByUnpaidTimeOver(int unpaidTimeMinutes);

    long updateStateToCancelByNoShowTimeOver(int noShowTimeMinutes);

}
