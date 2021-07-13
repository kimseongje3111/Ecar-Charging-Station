package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.books.PaymentRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.books.ReserveRequestDto;
import com.ecar.servicestation.modules.ecar.dto.response.books.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationNotFoundException;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.factory.ReservationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.ecar.service.ECarReservationService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.factory.CarFactory;
import com.ecar.servicestation.modules.user.factory.UserFactory;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ECarReservationApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    UserFactory userFactory;

    @Autowired
    CarFactory carFactory;

    @Autowired
    ECarStationFactory eCarStationFactory;

    @Autowired
    ReservationFactory reservationFactory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    ChargerRepository chargerRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ECarReservationService eCarReservationService;

    private static final String BASE_URL_RESERVE = "/ecar/reserve";

    private Car car;

    @BeforeEach
    void beforeEach() {
        RegisterCarRequestDto registerCarRequest = new RegisterCarRequestDto();
        registerCarRequest.setCarModel("소나타");
        registerCarRequest.setCarModel("2020");
        registerCarRequest.setCarType("중형");
        registerCarRequest.setCarNumber("99수9999");

        this.car = carFactory.createCar(withLoginAccount.getAccount(), registerCarRequest);

        eCarStationFactory.createStationAndAddCharger(2, 2);
        eCarStationFactory.createStationAndAddCharger(3, 3);
    }

    @AfterEach
    void afterEach() {
        withLoginAccount.getAccount().getMyCars().clear();

        carRepository.deleteAll();
        stationRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("[충전기 예약 시간 테이블 조회]정상 처리")
    public void find_charger_scheduled_time_table_success() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_RESERVE + "/" + charger.getId())
                                .param("day", "0")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data.timeTable").isNotEmpty());
    }

    @Test
    @DisplayName("[충전기 예약]정상 처리")
    public void reserve_charger_success() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Given
        ReserveRequestDto reserveRequest = new ReserveRequestDto();
        reserveRequest.setChargerId(charger.getId());
        reserveRequest.setCarId(car.getId());
        reserveRequest.setStart(start);
        reserveRequest.setEnd(end);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_RESERVE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reserveRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data").isNotEmpty());

        // Then(2)
        List<ReservationTable> stand_by =
                reservationRepository.findAllByChargerAndStateAndBetweenDateTime(charger.getId(), ReservationState.STAND_BY, start, end);

        assertThat(stand_by.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("[충전기 예약]실패 - 이미 완료된 예약 요청")
    public void reserve_charger_failed_by_already_confirmed_reservation() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Base(2)
        reservationFactory.createReservation(withLoginAccount.getAccount(), car.getId(), charger.getId(), start, end);

        // Given
        ReserveRequestDto reserveRequest = new ReserveRequestDto();
        reserveRequest.setChargerId(charger.getId());
        reserveRequest.setCarId(car.getId());
        reserveRequest.setStart(start);
        reserveRequest.setEnd(end);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_RESERVE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reserveRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1008));
    }

    @Test
    @DisplayName("[충전기 에약 결제]정상 처리")
    public void pay_reservation_fares_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        userFactory.cashInit(account, 10000);

        // Base(2)
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Base(3)
        ReservationTable reservation = reservationFactory.createReservation(account, car.getId(), charger.getId(), start, end);

        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setReservationId(reservation.getId());
        paymentRequest.setUsedCashPoint(0);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_RESERVE + "/payment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data.reserveTitle").isNotEmpty());

        // Then(2)
        reservation = reservationRepository.findById(reservation.getId()).orElseThrow(CReservationNotFoundException::new);
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        assertThat(reservation.getReserveState()).isEqualTo(ReservationState.PAYMENT);
        assertThat(account.getCash()).isEqualTo(10000 - (reservation.getReserveFares() - reservation.getUsedCashPoint()));
    }

    @Test
    @DisplayName("[충전기 에약 결제]실패 - 보유 결제 금액 또는 포인트 부족")
    public void pay_reservation_fares_failed_by_not_available_cash_or_point() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        userFactory.cashInit(account, 1000);

        // Base(2)
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Base(3)
        ReservationTable reservation = reservationFactory.createReservation(account, car.getId(), charger.getId(), start, end);

        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setReservationId(reservation.getId());
        paymentRequest.setUsedCashPoint(0);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_RESERVE + "/payment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(paymentRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1011));
    }

    @Test
    @DisplayName("[충전기 예약 취소]정상 처리")
    public void cancel_charger_reservation_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        userFactory.cashInit(account, 10000);

        // Base(2)
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Base(3)
        ReservationTable reservation = reservationFactory.confirmReservation(
                reservationFactory.createReservation(account, car.getId(), charger.getId(), start, end)
        );

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(BASE_URL_RESERVE)
                                .param("reserveTitle", reservation.getReserveTitle())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data").isNotEmpty());

        // Then(2)
        reservation = reservationRepository.findById(reservation.getId()).orElseThrow(CReservationNotFoundException::new);
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        assertThat(reservation.getReserveState()).isEqualTo(ReservationState.CANCEL);
        assertThat(account.getCash()).isEqualTo(10000 + (reservation.getReserveFares() - reservation.getUsedCashPoint()));
    }

    private LocalDateTime getDataTimeAfter2HourFromNow(long chargerId) {
        ChargerTimeTableDto chargerTimeTable = eCarReservationService.getChargerTimeTable(chargerId, 0);
        List<LocalDateTime> timeList = chargerTimeTable.getTimeTable().keySet().stream().sorted().collect(Collectors.toList());

        return timeList.get(0).plusHours(2);
    }

}