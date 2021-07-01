package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.NotificationRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.PaymentRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.ReserveRequestDto;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.dto.response.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.dto.response.ReserveResponseDto;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CReservationCancelFailedException;
import com.ecar.servicestation.modules.ecar.exception.CReservationNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CReserveFailedException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.exception.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserCashFailedException;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.ecar.servicestation.modules.ecar.domain.ReservationState.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ECarReservationService {

    private static final int CHARGER_TYPE_SLOW = 1;
    private static final int CHARGER_TYPE_FAST = 2;
    private static final int CHARGER_TYPE_SLOW_KW = 7;
    private static final int CHARGER_TYPE_FAST_KW = 50;

    private final UserRepository userRepository;
    private final ChargerRepository chargerRepository;
    private final ReservationRepository reservationRepository;
    private final AppProperties properties;
    private final ModelMapper modelMapper;

    public ChargerTimeTableDto getChargerTimeTable(long id, int day) {
        if (!chargerRepository.existsById(id)) {
            throw new CChargerNotFoundException();
        }

        LocalDateTime target = getTargetDateTime(day);
        LocalDateTime targetEnd = LocalDateTime.of(target.toLocalDate(), LocalTime.MAX);

        Map<LocalDateTime, Boolean> timeTableOfDay = makeTimeTable(target);
        List<ReservationTable> confirmed = reservationRepository.findAllByChargerAndStateAndBetweenDateTime(id, PAYMENT, target, targetEnd);

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
    public ReserveResponseDto reserveCharger(ReserveRequestDto request) {
        if (!isValidReserveRequest(request)) {
            throw new CReserveFailedException();
        }

        // 예약 테이블 확인 //

        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        List<ReservationTable> standBy = reservationRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), STAND_BY, start, end);
        List<ReservationTable> confirmed = reservationRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), PAYMENT, start, end);

        if (standBy.size() != 0 || confirmed.size() != 0) {
            throw new CReserveFailedException();
        }

        // 예약 생성 //

        Account account = getUserBasicInfo();
        Charger charger = chargerRepository.findById(request.getChargerId()).orElseThrow(CChargerNotFoundException::new);
        Car car = account.getMyCars().stream().filter(myCar -> myCar.getId().equals(request.getCarId())).findAny().orElseThrow(CCarNotFoundException::new);

        ReservationTable reserveItem =
                reservationRepository.save(
                        ReservationTable.builder()
                                .reservedAt(LocalDateTime.now())
                                .chargeStartDateTime(start)
                                .chargeEndDateTime(end)
                                .account(account)
                                .car(car)
                                .charger(charger)
                                .build()
                );

        // 요금 계산 //

        if (charger.getType() == CHARGER_TYPE_SLOW) {
            reserveItem.applyReservation(properties.getSlowChargingFares() * CHARGER_TYPE_SLOW_KW);

        } else if (charger.getType() == CHARGER_TYPE_FAST) {
            reserveItem.applyReservation(properties.getFastChargingFares() * CHARGER_TYPE_FAST_KW);
        }

        return getReserveResponse(account.getUsername(), reserveItem);
    }

    @Transactional
    public ReservationStatementDto paymentIn(PaymentRequestDto request) {
        Account account = getUserBasicInfo();
        ReservationTable reserveItem = reservationRepository.findReservationWithChargerAndCarById(request.getReservationId());

        if (reserveItem == null) {
            throw new CReservationNotFoundException();
        }

        if (!canPayment(account, reserveItem, request)) {
            throw new CUserCashFailedException();
        }

        int fares = reserveItem.getReserveFares();
        int usedCashPoint = request.getUsedCashPoint();

        if (usedCashPoint > 0) {
            account.paymentCashPoint(usedCashPoint);
            fares -= usedCashPoint;
        }

        account.paymentOrRefundCash(fares);
        reserveItem.confirmReservation(usedCashPoint);

        return getReservationStatement(account.getUsername(), reserveItem, 0);
    }

    @Transactional
    public ReservationStatementDto cancelReservation(String reserveTitle) {
        Account account = getUserBasicInfo();
        ReservationTable reserveItem = reservationRepository.findReservationWithChargerAndCarByReserveTitle(reserveTitle);

        if (reserveItem == null) {
            throw new CReservationNotFoundException();
        }

        if (reserveItem.getChargeStartDateTime().isBefore(LocalDateTime.now())) {
            throw new CReservationCancelFailedException();
        }

        int usedCashPoint = reserveItem.getUsedCashPoint();
        int fares = reserveItem.getReserveFares();
        int cancellationFee = calCancellationFee(reserveItem);

        if (usedCashPoint > 0) {
            account.chargingCashPoint(usedCashPoint);
            fares -= usedCashPoint;
        }

        account.chargingCash(fares - cancellationFee);
        reserveItem.cancelReservation();

        return getReservationStatement(account.getUsername(), reserveItem, cancellationFee);
    }

    @Transactional
    public void setNotification(NotificationRequestDto request) {
        ReservationTable reserveItem = reservationRepository.findReservationTableByReserveTitle(request.getReserveTitle());

        if (reserveItem == null) {
            throw new CReservationNotFoundException();
        }

        if (request.getIsOn()) {
            reserveItem.setOnNotification(request.getMinutes());
        } else {
            reserveItem.setOffNotification();
        }
    }

    private int calCancellationFee(ReservationTable reserveItem) {
        // 취소 수수료 계산 //

        LocalDateTime start = reserveItem.getChargeStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        return start.minusMinutes(30).isAfter(now) ? 0 : (int) (reserveItem.getReserveFares() * 0.2);
    }

    private ReservationStatementDto getReservationStatement(String username, ReservationTable reserveItem, int cancellationFee) {
        ReservationStatementDto statement = modelMapper.map(reserveItem, ReservationStatementDto.class);
        statement.setUserName(username);
        statement.setCarNumber(reserveItem.getCar().getCarNumber());
        statement.setCharger(reserveItem.getCharger());
        statement.setState(reserveItem.getReserveState().name());
        statement.setPaidCash(reserveItem.getReserveFares() - reserveItem.getUsedCashPoint());
        statement.setCancellationFee(cancellationFee);

        return statement;
    }

    private boolean canPayment(Account account, ReservationTable reservation, PaymentRequestDto request) {
        return request.getUsedCashPoint() <= account.getCashPoint()
                && request.getUsedCashPoint() <= reservation.getReserveFares()
                && reservation.getReserveFares() - request.getUsedCashPoint() <= account.getCash();
    }

    private ReserveResponseDto getReserveResponse(String userName, ReservationTable reserveItem) {
        ReserveResponseDto response = new ReserveResponseDto();
        response.setReservationId(reserveItem.getId());
        response.setUserName(userName);
        response.setCarNumber(reserveItem.getCar().getCarNumber());
        response.setChargerId(reserveItem.getCharger().getId());
        response.setReservedAt(reserveItem.getReservedAt());
        response.setState(reserveItem.getReserveState().name());
        response.setFares(reserveItem.getReserveFares());

        return response;
    }

    private boolean isValidReserveRequest(ReserveRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        return start.isBefore(end)
                && start.getMinute() % 30 == 0
                && end.getMinute() % 30 == 0
                && start.minusHours(1).isAfter(now);        // 1시간 전
    }

    private ChargerTimeTableDto getChargerTimeTable(long id, LocalDateTime target, Map<LocalDateTime, Boolean> timeTableOfDay) {
        ChargerTimeTableDto timeTable = new ChargerTimeTableDto();
        timeTable.setChargerId(id);
        timeTable.setTargetDate(target.toLocalDate());
        timeTable.setTimeTable(timeTableOfDay);

        return timeTable;
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
        now = LocalDateTime.of(now.toLocalDate(), LocalTime.of(now.getHour(), now.getMinute(), 0));

        if (day != 0) {
            now = LocalDateTime.of(now.toLocalDate().plusDays(day), LocalTime.MIDNIGHT);
        }

        return now;
    }

    private Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }
}
