package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.ReserveRequest;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerTimeTable;
import com.ecar.servicestation.modules.ecar.dto.response.ReserveResponse;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CReserveFailedException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationTableRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ECarReservationService {

    private final ReservationTableRepository reservationTableRepository;
    private final UserRepository userRepository;
    private final ChargerRepository chargerRepository;
    private final AppProperties properties;

    private final int CHARGER_TYPE_SLOW = 1;
    private final int CHARGER_TYPE_FAST = 2;

    public ChargerTimeTable getChargerTimeTable(long id, int day) {
        LocalDateTime target = getTargetDateTime(day);
        LocalDateTime targetEnd = LocalDateTime.of(target.toLocalDate(), LocalTime.MAX);
        Map<LocalDateTime, Boolean> timeTableOfDay = makeTimeTable(target);

        List<ReservationTable> confirmed =
                reservationTableRepository.findAllByChargerAndStateAndBetweenDateTime(id, ReservationState.PAYMENT, target, targetEnd);

        for (ReservationTable current : confirmed) {
            LocalDateTime start = current.getChargeStartDateTime();
            LocalDateTime end = current.getChargeEndDateTime();

            while (start.isBefore(end) || start.isEqual(end)) {
                timeTableOfDay.putIfAbsent(start, false);
                start = start.plusMinutes(30);
            }
        }

        return getChargerTimeTable(id, target, timeTableOfDay);
    }

    @Transactional
    public ReserveResponse reserveCharger(ReserveRequest request) {
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        if (!start.isBefore(end) || start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0) {
            throw new CReserveFailedException();
        }

        List<ReservationTable> standBy =
                reservationTableRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), ReservationState.STAND_BY, start, end);

        List<ReservationTable> confirmed =
                reservationTableRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), ReservationState.PAYMENT, start, end);

        if (standBy.size() == 0 && confirmed.size() == 0) {
            Account account = getUserBasicInfo();
            Charger charger = chargerRepository.findById(request.getChargerId()).orElseThrow(CChargerNotFoundException::new);

            ReservationTable reserveItem =
                    reservationTableRepository.save(
                            ReservationTable.builder()
                                    .reservedAt(LocalDateTime.now())
                                    .chargeStartDateTime(start)
                                    .chargeEndDateTime(end)
                                    .account(account)
                                    .charger(charger)
                                    .build()
                    );

            if (charger.getType() == CHARGER_TYPE_SLOW) {
                reserveItem.applyReservation(properties.getSlowChargingFares());

            } else if (charger.getType() == CHARGER_TYPE_FAST) {
                reserveItem.applyReservation(properties.getFastChargingFares());
            }

            return getReserveResponse(request, account, reserveItem);
        }

        throw new CReserveFailedException();
    }

    private Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    private ReserveResponse getReserveResponse(ReserveRequest request, Account account, ReservationTable reserveItem) {
        ReserveResponse reserveResponse = new ReserveResponse();
        reserveResponse.setChargerId(request.getChargerId());
        reserveResponse.setUserName(account.getUsername());
        reserveResponse.setReservationId(reserveItem.getId());
        reserveResponse.setReservedAt(reserveItem.getReservedAt());
        reserveResponse.setState(reserveItem.getReserveState().name());
        reserveResponse.setFares(reserveItem.getReserveFares());

        return reserveResponse;
    }

    private ChargerTimeTable getChargerTimeTable(long id, LocalDateTime target, Map<LocalDateTime, Boolean> timeTableOfDay) {
        ChargerTimeTable chargerTimeTable = new ChargerTimeTable();
        chargerTimeTable.setChargerId(id);
        chargerTimeTable.setTargetDate(target.toLocalDate());
        chargerTimeTable.setTimeTable(timeTableOfDay);

        return chargerTimeTable;
    }

    private Map<LocalDateTime, Boolean> makeTimeTable(LocalDateTime now) {
        Map<LocalDateTime, Boolean> timeTableOfDay = new HashMap<>();
        LocalDateTime max = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        while (now.isBefore(max)) {
            timeTableOfDay.put(now, true);
            now = now.plusMinutes(30);
        }

        return timeTableOfDay;
    }

    private LocalDateTime getTargetDateTime(int day) {
        LocalDateTime now = LocalDateTime.now();
        now = now.getMinute() < 30 ? now.plusMinutes(30 - now.getMinute()) : now.plusMinutes(60 - now.getMinute());
        ;
        now = LocalDateTime.of(now.toLocalDate(), LocalTime.of(now.getHour(), now.getMinute(), 0));

        if (day != 0) {
            now = LocalDateTime.of(now.toLocalDate().plusDays(day), LocalTime.MIDNIGHT);
        }

        return now;
    }
}
