package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.app.AppProperties;
import com.ecar.servicestation.infra.bank.service.BankService;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.request.books.PaymentRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.books.ReserveRequestDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.MaxEndDateTimeDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.SimpleReservationInfoDto;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationCancelFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.books.CReserveFailedException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.exception.banks.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.cars.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.banks.CUserCashFailedException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final BankService bankService;
    private final AppProperties properties;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public ChargerTimeTableDto getChargerTimeTable(long chargerId, int day) {
        if (!chargerRepository.existsById(chargerId)) {
            throw new CChargerNotFoundException();
        }

        if (LocalDateTime.now().getHour() == 23 && day == 0) {
            return getChargerTimeTable(chargerId, LocalDateTime.now(), new HashMap<>());
        }

        LocalDateTime target = getTargetDateTime(day);
        LocalDateTime targetEnd = LocalDateTime.of(target.toLocalDate(), LocalTime.MAX);
        Map<String, Boolean> timeTableOfDay = makeTimeTableOfDay(target);

        List<ReservationTable> unpaidItems =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(chargerId, STAND_BY, target, targetEnd);

        List<ReservationTable> reservedItems =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(chargerId, PAYMENT, target, targetEnd);

        for (ReservationTable unpaidItem : unpaidItems) {
            LocalDateTime start = unpaidItem.getChargeStartDateTime();
            LocalDateTime end = unpaidItem.getChargeEndDateTime();

            while (start.isBefore(end) || start.isEqual(end)) {
                timeTableOfDay.put(start.format(DateTimeFormatter.ofPattern("HH:mm")), false);
                start = start.plusMinutes(30);
            }
        }

        for (ReservationTable reservedItem : reservedItems) {
            LocalDateTime start = reservedItem.getChargeStartDateTime();
            LocalDateTime end = reservedItem.getChargeEndDateTime();

            while (start.isBefore(end) || start.isEqual(end)) {
                timeTableOfDay.put(start.format(DateTimeFormatter.ofPattern("HH:mm")), false);
                start = start.plusMinutes(30);
            }
        }

        return getChargerTimeTable(chargerId, target, timeTableOfDay);
    }

    public MaxEndDateTimeDto getMaxEndDateTimeFromStartDateTime(long chargerId, String startDateTIme) {
        if (!chargerRepository.existsById(chargerId)) {
            throw new CChargerNotFoundException();
        }

        // 충전 시작 시간 이후로 최대 8시간 //

        LocalDateTime startDateTime = LocalDateTime.parse(startDateTIme, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        LocalDateTime maxEndDateTime = startDateTime.plusHours(8);

        List<ReservationTable> reservedItems =
                reservationRepository.findAllByChargerAndReservedStateAndStartBetweenDateTime(chargerId, startDateTime, maxEndDateTime);

        if (reservedItems.size() != 0) {
            maxEndDateTime = reservedItems.get(0).getChargeStartDateTime();
        }

        // 시간당 요금 계산 //

        int faresByChargerType = 0;
        Charger charger = chargerRepository.findById(chargerId).orElseThrow(CChargerNotFoundException::new);

        if (charger.getType() == CHARGER_TYPE_SLOW) {
            faresByChargerType = properties.getSlowChargingFares() * CHARGER_TYPE_SLOW_KW;

        } else if (charger.getType() == CHARGER_TYPE_FAST) {
            faresByChargerType = properties.getFastChargingFares() * CHARGER_TYPE_FAST_KW;
        }

        return getMaxEndDateTime(chargerId, startDateTime, maxEndDateTime, faresByChargerType);
    }

    @Transactional
    public ReservationStatementDto reserveCharger(ReserveRequestDto request) {
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
                                .isSentNotificationOfReservationStart(false)
                                .isSentNotificationOfChargingEnd(false)
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

        return getReservationStatement(account.getName(), newReservation, 0);
    }

    public SimpleReservationInfoDto getReservationInfo(long reservationId) {
        Account account = getLoginUserContext();
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndCarById(reservationId);

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        Charger charger = chargerRepository.findChargerWithStationById(reservedItem.getCharger().getId());
        Station station = charger.getStation();

        return getSimpleReservationInfo(account.getName(), station.getStationName(), reservedItem);
    }

    @Transactional
    public ReservationStatementDto paymentInReservation(PaymentRequestDto request) {
        Account account = getLoginUserContext();
        Bank mainUsedBank = account.getMainUsedBank();
        ReservationTable reservedItem = reservationRepository.findReservationWithChargerAndCarById(request.getReservationId());

        if (reservedItem == null) {
            throw new CReservationNotFoundException();
        }

        if (mainUsedBank == null) {
            throw new CBankNotFoundException();
        }

        if (!passwordEncoder.matches(request.getPaymentPassword(), mainUsedBank.getPaymentPassword()) || !canPayment(account, reservedItem, request)) {
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

        } else {
            now = now.plusHours(1);
        }

        return now;
    }

    private Map<String, Boolean> makeTimeTableOfDay(LocalDateTime now) {
        Map<String, Boolean> timeTableOfDay = new HashMap<>();
        LocalDateTime max = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        while (now.isBefore(max)) {
            timeTableOfDay.put(now.format(DateTimeFormatter.ofPattern("HH:mm")), true);
            now = now.plusMinutes(30);
        }

        return timeTableOfDay;
    }

    private ChargerTimeTableDto getChargerTimeTable(long chargerId, LocalDateTime target, Map<String, Boolean> timeTableOfDay) {
        ChargerTimeTableDto timeTable = new ChargerTimeTableDto();
        timeTable.setChargerId(chargerId);
        timeTable.setTargetDate(target.toLocalDate());
        timeTable.setTimeTable(timeTableOfDay);

        return timeTable;
    }

    private MaxEndDateTimeDto getMaxEndDateTime(long chargerId, LocalDateTime start, LocalDateTime end, int faresByChargerType) {
        MaxEndDateTimeDto maxEndDateTime = new MaxEndDateTimeDto();
        maxEndDateTime.setChargerId(chargerId);
        maxEndDateTime.setTargetDateTime(start);
        maxEndDateTime.setMaxEndDateTime(end);
        maxEndDateTime.setFaresPerHour(faresByChargerType);

        return maxEndDateTime;
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

    private SimpleReservationInfoDto getSimpleReservationInfo(String userName, String stationName, ReservationTable reservedItem) {
        SimpleReservationInfoDto reservationInfo = new SimpleReservationInfoDto();
        reservationInfo.setUserName(userName);
        reservationInfo.setStationName(stationName);
        reservationInfo.setChargerName(reservedItem.getCharger().getChargerName());
        reservationInfo.setCarNumber(reservedItem.getCar().getCarNumber());
        reservationInfo.setChargeStartDateTime(reservedItem.getChargeStartDateTime());
        reservationInfo.setChargeEndDateTime(reservedItem.getChargeEndDateTime());
        reservationInfo.setFares(reservedItem.getReserveFares());

        return reservationInfo;
    }

    private boolean canPayment(Account account, ReservationTable reservedItem, PaymentRequestDto request) {
        if (request.getUsedCashPoint() <= account.getCashPoint()
                && request.getUsedCashPoint() <= reservedItem.getReserveFares()) {

            if (reservedItem.getReserveFares() - request.getUsedCashPoint() > account.getCash()) {
                Bank mainUsedBank = account.getMainUsedBank();

                int ussdCash = reservedItem.getReserveFares() - request.getUsedCashPoint();
                int cashDiff = ussdCash - account.getCash();
                int chargingAmount = ((cashDiff / 10000) + 1) * 10000;

                bankService.withdrawFrom(mainUsedBank.getBankName(), mainUsedBank.getBankAccountNumber(), mainUsedBank.getBankAccountAccessToken(), chargingAmount);
                account.chargingCash(chargingAmount);
            }

            return true;
        }

        return false;

    }

    private ReservationStatementDto getReservationStatement(String userName, ReservationTable reservedItem, int cancellationFee) {
        ReservationStatementDto statement = modelMapper.map(reservedItem, ReservationStatementDto.class);
        statement.setUserName(userName);
        statement.setReservationId(reservedItem.getId());
        statement.setChargerId(reservedItem.getCharger().getId());
        statement.setCarNumber(reservedItem.getCar().getCarNumber());
        statement.setState(reservedItem.getReserveState().name());
        statement.setPaidCash(reservedItem.getReserveFares() - reservedItem.getUsedCashPoint());
        statement.setCancellationFee(cancellationFee);

        if (reservedItem.getReserveTitle() != null) {
            statement.setReserveTitle(reservedItem.getReserveTitle());
        }

        if (reservedItem.getReserveState().equals(ReservationState.STAND_BY)) {
            statement.setPaidCash(0);
        }

        return statement;
    }

    private int calCancellationFee(ReservationTable reservedItem) {

        // 취소 수수료 계산 //

        LocalDateTime start = reservedItem.getChargeStartDateTime();
        LocalDateTime now = LocalDateTime.now();

        return start.minusHours(1).isAfter(now) ? 0 : (int) (reservedItem.getReserveFares() * 0.1);
    }

}
