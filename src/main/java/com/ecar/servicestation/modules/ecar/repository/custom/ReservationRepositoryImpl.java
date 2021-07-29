package com.ecar.servicestation.modules.ecar.repository.custom;

import com.ecar.servicestation.infra.querydsl.Querydsl4RepositorySupport;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import java.time.LocalDateTime;
import java.util.List;

import static com.ecar.servicestation.modules.ecar.domain.QCharger.*;
import static com.ecar.servicestation.modules.ecar.domain.QReservationTable.*;
import static com.ecar.servicestation.modules.ecar.domain.ReservationState.*;
import static com.ecar.servicestation.modules.user.domain.QAccount.*;
import static com.ecar.servicestation.modules.user.domain.QCar.*;
import static com.ecar.servicestation.modules.user.domain.QDeviceToken.*;

public class ReservationRepositoryImpl extends Querydsl4RepositorySupport implements ReservationRepositoryCustom {

    public ReservationRepositoryImpl() {
        super(ReservationTable.class);
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
    public ReservationTable findReservationWithChargerAndAccountByReserveTitle(String reserveTitle) {
        return selectFrom(reservationTable)
                .join(reservationTable.charger, charger).fetchJoin()
                .join(reservationTable.account, account).fetchJoin()
                .join(account.deviceToken, deviceToken).fetchJoin()
                .where(reservationTable.reserveTitle.eq(reserveTitle))
                .fetchOne();
    }

    @Override
    public List<ReservationTable> findAllWithChargerAndCarByAccountAndNotCancel(long accountId) {
        return selectFrom(reservationTable)
                .join(reservationTable.account, account)
                .join(reservationTable.charger, charger).fetchJoin()
                .join(reservationTable.car, car).fetchJoin()
                .where(
                        account.id.eq(accountId),
                        reservationTable.reserveState.eq(STAND_BY)
                                .or(reservationTable.reserveState.eq(PAYMENT))
                                .or(reservationTable.reserveState.eq(CHARGING))
                )
                .fetch();
    }

    @Override
    public List<ReservationTable> findAllWithChargerAndCarByAccountAndComplete(long accountId) {
        return selectFrom(reservationTable)
                .join(reservationTable.account, account)
                .join(reservationTable.charger, charger).fetchJoin()
                .join(reservationTable.car, car).fetchJoin()
                .where(
                        account.id.eq(accountId),
                        reservationTable.reserveState.eq(COMPLETE)
                )
                .fetch();
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
    public List<ReservationTable> findAllWithAccountByPaymentStateAndCanNotificationOfReservationStart() {
        return selectFrom(reservationTable)
                .join(reservationTable.account, account).fetchJoin()
                .join(account.deviceToken, deviceToken).fetchJoin()
                .where(
                        reservationTable.reserveState.eq(PAYMENT),
                        reservationTable.isSentNotificationOfReservationStart.isFalse(),
                        account.isOnNotificationOfReservationStart.isTrue()
                )
                .fetch();
    }

    @Override
    public List<ReservationTable> findAllWithAccountByChargingStateAndCanNotificationOfChargingEnd() {
        return selectFrom(reservationTable)
                .join(reservationTable.account, account).fetchJoin()
                .join(account.deviceToken, deviceToken).fetchJoin()
                .where(
                        reservationTable.reserveState.eq(CHARGING),
                        reservationTable.isSentNotificationOfChargingEnd.isFalse(),
                        account.isOnNotificationOfChargingEnd.isTrue()
                )
                .fetch();
    }

    @Override
    public long updateStateToCancelByUnpaidTimeOver(int unpaidTimeMinutes) {
        long count =
                update(reservationTable)
                        .set(reservationTable.reserveState, CANCEL)
                        .where(
                                reservationTable.reserveState.eq(STAND_BY),
                                isOverUnpaidTime(unpaidTimeMinutes)
                        )
                        .execute();

        if (count != 0) {
            getEntityManager().clear();
        }

        return count;
    }

    @Override
    public long updateStateToCancelByNoShowTimeOver(int noShowTimeMinutes) {
        long count =
                update(reservationTable)
                        .set(reservationTable.reserveState, CANCEL)
                        .where(
                                reservationTable.reserveState.eq(PAYMENT),
                                isOverNoShowTime(noShowTimeMinutes)
                        )
                        .execute();

        if (count != 0) {
            getEntityManager().clear();
        }

        return 0;
    }

    private BooleanExpression isOverUnpaidTime(int outstandingTimeMinutes) {
        return reservationTable.reservedAt.before(LocalDateTime.now().minusMinutes(outstandingTimeMinutes));
    }

    private BooleanExpression isOverNoShowTime(int noShowTimeMinutes) {
        return reservationTable.chargeStartDateTime.before(LocalDateTime.now().minusMinutes(noShowTimeMinutes));
    }

}
