package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.user.domain.Account;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    List<ReservationTable> findAllByChargerAndStateAndBetweenDateTime(long chargerId, ReservationState state, LocalDateTime start, LocalDateTime end);

    long updateStateFromStandByToCancelByOutstandingTime(int outstandingTimeMinutes);

    ReservationTable findReservationWithChargerAndCarById(long reservationId);

    ReservationTable findReservationWithChargerAndCarByReserveTitle(String reserveTitle);

    List<ReservationTable> findAllWithChargerAndCarByAccountAndState(long accountId, ReservationState state);
}
