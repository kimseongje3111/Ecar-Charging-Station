package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QReservationTable.*;

public class ReservationTableRepositoryImpl extends Querydsl4RepositorySupport implements ReservationTableRepositoryCustom {

    public ReservationTableRepositoryImpl() {
        super(ReservationTable.class);
    }

    @Override
    public List<ReservationTable> findAllByChargerAndStateAndBetweenDateTime(long chargerId, ReservationState state, LocalDateTime start, LocalDateTime end) {
        return selectFrom(reservationTable)
                .join(reservationTable.charger, charger)
                .where(
                        charger.id.eq(chargerId),
                        reservationTable.reserveState.eq(state),
                        reservationTable.chargeStartDateTime.eq(start)
                                .or(reservationTable.chargeStartDateTime.between(start, end))
                                .or(reservationTable.chargeEndDateTime.eq(start))
                                .or(reservationTable.chargeEndDateTime.between(start, end))
                )
                .fetch();
    }

}
