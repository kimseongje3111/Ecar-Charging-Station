package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationTableRepositoryCustom {

    List<ReservationTable> findAllByChargerAndStateAndBetweenDateTime(long chargerId, ReservationState state, LocalDateTime start, LocalDateTime end);

    long updateStateFromStandByToCancelByOutstandingTime(int outstandingTimeMinutes);

    ReservationTable findReservationWithChargerAndCarById(long reservationId);

    ReservationTable findReservationWithChargerAndCarByReserveTitle(String reserveTitle);
}
