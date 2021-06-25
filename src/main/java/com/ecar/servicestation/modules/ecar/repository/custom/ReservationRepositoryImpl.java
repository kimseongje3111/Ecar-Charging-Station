package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.QAccount;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import java.time.LocalDateTime;
import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QReservationTable.*;
import static com.ecar.servicestation.modules.ecar.domain.ReservationState.*;
import static com.ecar.servicestation.modules.user.domain.QAccount.*;
import static com.ecar.servicestation.modules.user.domain.QCar.*;

public class ReservationRepositoryImpl extends Querydsl4RepositorySupport implements ReservationRepositoryCustom {

    public ReservationRepositoryImpl() {
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

    @Override
    public long updateStateFromStandByToCancelByOutstandingTime(int outstandingTimeMinutes) {
        long count =
                update(reservationTable)
                        .set(reservationTable.reserveState, CANCEL)
                        .where(
                                reservationTable.reserveState.eq(STAND_BY),
                                isOverOutstandingTime(outstandingTimeMinutes)
                        )
                        .execute();

        if (count != 0) {
            getEntityManager().clear();
        }

        return count;
    }

    @Override
    public ReservationTable findReservationWithChargerAndCarById(long reservationId) {
        return selectFrom(reservationTable)
                .join(reservationTable.charger, charger).fetchJoin()
                .join(reservationTable.car, car).fetchJoin()
                .where(reservationTable.id.eq(reservationId))
                .fetchOne();
    }

    @Override
    public ReservationTable findReservationWithChargerAndCarByReserveTitle(String reserveTitle) {
        return selectFrom(reservationTable)
                .join(reservationTable.charger, charger).fetchJoin()
                .join(reservationTable.car, car).fetchJoin()
                .where(reservationTable.reserveTitle.eq(reserveTitle))
                .fetchOne();
    }

    @Override
    public List<ReservationTable> findAllWithChargerAndCarByAccountAndState(long accountId, ReservationState state) {
        JPAQuery<ReservationTable> query =
                selectFrom(reservationTable)
                        .join(reservationTable.account, account)
                        .join(reservationTable.charger, charger).fetchJoin()
                        .join(reservationTable.car, car).fetchJoin();

        if (state.equals(PAYMENT)) {
            return query
                    .where(
                            account.id.eq(accountId),
                            reservationTable.reserveState.eq(state)
                                    .or(reservationTable.reserveState.eq(STAND_BY))
                    )
                    .fetch();

        } else {
            return query
                    .where(
                            account.id.eq(accountId),
                            reservationTable.reserveState.eq(state)
                    )
                    .fetch();
        }
    }

    private BooleanExpression isOverOutstandingTime(int outstandingTimeMinutes) {
        return reservationTable.reservedAt.before(LocalDateTime.now().minusMinutes(outstandingTimeMinutes));
    }
}
