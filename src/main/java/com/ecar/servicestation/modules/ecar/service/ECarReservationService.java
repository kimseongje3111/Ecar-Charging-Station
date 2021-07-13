package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.NotificationRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.books.PaymentRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.books.ReserveRequestDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ReserveResponseDto;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationCancelFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReserveFailedException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.exception.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.banks.CUserCashFailedException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
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

    public ChargerTimeTableDto getChargerTimeTable(long chargerId, int day) {
        if (!chargerRepository.existsById(chargerId)) {
            throw new CChargerNotFoundException();
        }

        LocalDateTime target = getTargetDateTime(day);
        LocalDateTime targetEnd = LocalDateTime.of(target.toLocalDate(), LocalTime.MAX);
        Map<LocalDateTime, Boolean> timeTableOfDay = makeTimeTableOfDay(target);

        List<ReservationTable> reservedItems =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(chargerId, PAYMENT, target, targetEnd);

        for (ReservationTable reservedItem : reservedItems) {
            LocalDateTime start = reservedItem.getChargeStartDateTime();
            LocalDateTime end = reservedItem.getChargeEndDateTime();

            while (start.isBefore(end) || start.isEqual(end)) {
                timeTableOfDay.putIfAbsent(start, false);
                start = start.plusMinutes(30);
            }
        }

        return getChargerTimeTable(chargerId, target, timeTableOfDay);
    }

    @Transactional
    public ReserveResponseDto reserveCharger(ReserveRequestDto request) {
        if (!isValidReserveRequest(request)) {
            throw new CReserveFailedException();
        }

        // 예약 테이블 확인 //

        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        List<ReservationTable> unpaidItems =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), STAND_BY, start, end);

        List<ReservationTable> reservedItems =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(request.getChargerId(), PAYMENT, start, end);

        if (unpaidItems.size() != 0 || reservedItems.size() != 0) {
            throw new CReserveFailedException();
        }

        // 예약 생성 //

        Account account = getLoginUserContext();
        Charger charger = chargerRepository.findById(request.getChargerId()).orElseThrow(CChargerNotFoundException::new);
        Car car = account.getMyCars().stream().filter(myCar -> myCar.getId().equals(request.getCarId())).findAny().orElseThrow(CCarNotFoundException::new);

        ReservationTable newReservation =
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

        int faresByChargerType = 0;

        if (charger.getType() == CHARGER_TYPE_SLOW) {
            faresByChargerType = properties.getSlowChargingFares() * CHARGER_TYPE_SLOW_KW;

        } else if (charger.getType() == CHARGER_TYPE_FAST) {
            faresByChargerType = properties.getFastChargingFares() * CHARGER_TYPE_FAST_KW;
        }

        newReservation.applyReservation(faresByChargerType);

        return getReserveResponse(account.getName(), newReservation);
    }

    @Transactional
    public ReservationStatementDto paymentInReservation(PaymentRequestDto request) {
        Account account = getLoginUserContext();
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndCarById(request.getReservationId());

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        if (!canPayment(account, reservedItem, request)) {
            throw new CUserCashFailedException();
        }

        int fares = reservedItem.getReserveFares();
        int usedCashPoint = request.getUsedCashPoint();

        if (usedCashPoint > 0) {
            account.paymentCashPoint(usedCashPoint);
            fares -= usedCashPoint;
        }

        account.paymentOrRefundCash(fares);
        reservedItem.confirmReservation(usedCashPoint);

        return getReservationStatement(account.getName(), reservedItem, 0);
    }

    @Transactional
    public ReservationStatementDto cancelReservation(String reserveTitle) {
        Account account = getLoginUserContext();
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndCarByReserveTitle(reserveTitle);

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        if (reservedItem.getChargeStartDateTime().isBefore(LocalDateTime.now())) {
            throw new CReservationCancelFailedException();
        }

        int usedCashPoint = reservedItem.getUsedCashPoint();
        int fares = reservedItem.getReserveFares();
        int cancellationFee = calCancellationFee(reservedItem);

        if (usedCashPoint > 0) {
            account.chargingCashPoint(usedCashPoint);
            fares -= usedCashPoint;
        }

        account.chargingCash(fares - cancellationFee);
        reservedItem.cancelReservation();

        return getReservationStatement(account.getName(), reservedItem, cancellationFee);
    }

    private Account getLoginUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
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

    private Map<LocalDateTime, Boolean> makeTimeTableOfDay(LocalDateTime now) {
        Map<LocalDateTime, Boolean> timeTableOfDay = new HashMap<>();
        LocalDateTime max = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        while (now.isBefore(max)) {
            timeTableOfDay.put(now, true);
            now = now.plusMinutes(30);
        }

        return timeTableOfDay;
    }

    private ChargerTimeTableDto getChargerTimeTable(long chargerId, LocalDateTime target, Map<LocalDateTime, Boolean> timeTableOfDay) {
        ChargerTimeTableDto timeTable = new ChargerTimeTableDto();
        timeTable.setChargerId(chargerId);
        timeTable.setTargetDate(target.toLocalDate());
        timeTable.setTimeTable(timeTableOfDay);

        return timeTable;
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

    private ReserveResponseDto getReserveResponse(String userName, ReservationTable reservedItem) {
        ReserveResponseDto response = new ReserveResponseDto();
        response.setReservationId(reservedItem.getId());
        response.setUserName(userName);
        response.setCarNumber(reservedItem.getCar().getCarNumber());
        response.setChargerId(reservedItem.getCharger().getId());
        response.setReservedAt(reservedItem.getReservedAt());
        response.setState(reservedItem.getReserveState().name());
        response.setFares(reservedItem.getReserveFares());

        return response;
    }

    private boolean canPayment(Account account, ReservationTable reservedItem, PaymentRequestDto request) {
        return request.getUsedCashPoint() <= account.getCashPoint()
                && request.getUsedCashPoint() <= reservedItem.getReserveFares()
                && reservedItem.getReserveFares() - request.getUsedCashPoint() <= account.getCash();
    }

    private ReservationStatementDto getReservationStatement(String username, ReservationTable reservedItem, int cancellationFee) {
        ReservationStatementDto statement = modelMapper.map(reservedItem, ReservationStatementDto.class);
        statement.setUserName(username);
        statement.setCarNumber(reservedItem.getCar().getCarNumber());
        statement.setCharger(reservedItem.getCharger());
        statement.setState(reservedItem.getReserveState().name());
        statement.setPaidCash(reservedItem.getReserveFares() - reservedItem.getUsedCashPoint());
        statement.setCancellationFee(cancellationFee);

        return statement;
    }

    private int calCancellationFee(ReservationTable reservedItem) {
        // 취소 수수료 계산 //

        LocalDateTime start = reservedItem.getChargeStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        return start.minusMinutes(30).isAfter(now) ? 0 : (int) (reservedItem.getReserveFares() * 0.2);
    }

}
