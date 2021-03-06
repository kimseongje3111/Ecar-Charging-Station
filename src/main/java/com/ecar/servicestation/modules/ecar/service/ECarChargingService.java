package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.infra.notification.service.PushMessageService;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.ChargerRequestDto;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationEndFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationStartFailedException;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.DeviceToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ECarChargingService {

    private static final int AVAILABLE_TIME_MINUTE_DELAY = 30;
    private static final int AVAILABLE_TIME_MINUTE_REFUND = 30;
    private static final int CHARGER_TYPE_SLOW = 1;
    private static final int CHARGER_TYPE_FAST = 2;
    private static final int CHARGER_TYPE_SLOW_KW = 7;
    private static final int CHARGER_TYPE_FAST_KW = 50;

    private final ReservationRepository reservationRepository;
    private final PushMessageService pushMessageService;
    private final AppProperties properties;

    @Transactional
    public void checkReservationAndStartCharging(ChargerRequestDto request) {
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndCarByReserveTitle(request.getReserveTitle());

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        if (!reservedItem.getCharger().getChargerNumber().equals(request.getChargerNumber())) {
            throw new CChargerNotFoundException();
        }

        if (!reservedItem.startCharging(AVAILABLE_TIME_MINUTE_DELAY)) {
            throw new CReservationStartFailedException();
        }
    }

    @Transactional
    public void checkReservationAndEndCharging(ChargerRequestDto request) {
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndAccountByReserveTitle(request.getReserveTitle());

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        Charger charger = reservedItem.getCharger();

        if (!charger.getChargerNumber().equals(request.getChargerNumber())) {
            throw new CChargerNotFoundException();
        }

        if (!reservedItem.endCharging()) {
            throw new CReservationEndFailedException();
        }

        // ?????? ?????? ??? ????????? ?????? //

        Account account = reservedItem.getAccount();

        if (account.isOnNotificationOfChargingEnd() && !reservedItem.isSentNotificationOfChargingEnd()) {
            DeviceToken deviceToken = account.getDeviceToken();
            String title = "?????? ?????? ??????";
            String body = "????????? ?????????????????????. ????????? ????????? ????????? ????????? ?????????.";

            pushMessageService.sendPushMessageTo(deviceToken.getDeviceUniqueToken(), title, body);
            reservedItem.setSentNotificationOfChargingEnd(true);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = reservedItem.getChargeEndDateTime();

        if (now.isBefore(end) && end.isAfter(now.plusMinutes(AVAILABLE_TIME_MINUTE_REFUND))) {
            int faresByChargerType = 0;

            if (charger.getType() == CHARGER_TYPE_SLOW) {
                faresByChargerType = (int) ((properties.getSlowChargingFares() * CHARGER_TYPE_SLOW_KW) * 0.5);

            } else if (charger.getType() == CHARGER_TYPE_FAST) {
                faresByChargerType = (int) ((properties.getFastChargingFares() * CHARGER_TYPE_FAST_KW) * 0.5);
            }

            account.chargingCashPoint(reservedItem.calRefundCashPoint(faresByChargerType));
        }
    }

}
