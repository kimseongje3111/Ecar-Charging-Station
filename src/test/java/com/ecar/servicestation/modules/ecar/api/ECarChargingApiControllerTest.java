package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationState;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.dto.request.ChargerRequestDto;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.factory.ReservationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.factory.CarFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ECarChargingApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    CarFactory carFactory;

    @Autowired
    ECarStationFactory eCarStationFactory;

    @Autowired
    ReservationFactory reservationFactory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    ChargerRepository chargerRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ObjectMapper objectMapper;

    private static final String BASE_URL_CHARGING = "/ecar/charge";

    private Car car;

    @BeforeEach
    void beforeEach() {
        withLoginAccount.init();
        eCarStationFactory.createStationAndAddCharger(1, 2);

        RegisterCarRequestDto registerCarRequest = new RegisterCarRequestDto();
        registerCarRequest.setCarModel("CAR01-MODEL");
        registerCarRequest.setCarModelYear("2021");
        registerCarRequest.setCarType("CAR01-TYPE");
        registerCarRequest.setCarNumber("99???9999");

        this.car = carFactory.createCar(withLoginAccount.getAccount(), registerCarRequest);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        stationRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("[?????? ?????? ??????]?????? ??????")
    public void start_charging_reservation_success() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = start.plusHours(1);

        // Base(2)
        ReservationTable reservation = reservationFactory.confirmReservation(
                reservationFactory.createReservation(withLoginAccount.getAccount(), car.getId(), charger.getId(), start, end)
        );

        // Given
        ChargerRequestDto chargerRequest = new ChargerRequestDto();
        chargerRequest.setChargerNumber(charger.getChargerNumber());
        chargerRequest.setReserveTitle(reservation.getReserveTitle());

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_CHARGING)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(chargerRequest))
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        reservation = reservationRepository.findReservationTableByReserveTitle(reservation.getReserveTitle());

        assertThat(reservation.getReserveState()).isEqualTo(ReservationState.CHARGING);
    }

    @Test
    @DisplayName("[?????? ?????? ??????]?????? - ????????? ?????? ?????? ?????? ?????? ??????")
    public void start_charging_reservation_failed_by_before_start_time() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        // Base(2)
        ReservationTable reservation = reservationFactory.confirmReservation(
                reservationFactory.createReservation(withLoginAccount.getAccount(), car.getId(), charger.getId(), start, end)
        );

        // Given
        ChargerRequestDto chargerRequest = new ChargerRequestDto();
        chargerRequest.setChargerNumber(charger.getChargerNumber());
        chargerRequest.setReserveTitle(reservation.getReserveTitle());

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_CHARGING)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(chargerRequest))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1013));
    }

    @Test
    @DisplayName("[?????? ?????? ??????]?????? ??????")
    public void finish_charging_reservation_success() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = start.plusHours(1);

        // Base(2)
        ReservationTable reservation = reservationFactory.createReservation(withLoginAccount.getAccount(), car.getId(), charger.getId(), start, end);
        reservation = reservationFactory.confirmReservation(reservation);
        reservation = reservationFactory.chargingReservation(reservation);

        // Given
        ChargerRequestDto chargerRequest = new ChargerRequestDto();
        chargerRequest.setChargerNumber(charger.getChargerNumber());
        chargerRequest.setReserveTitle(reservation.getReserveTitle());

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_CHARGING + "/finish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(chargerRequest))
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        reservation = reservationRepository.findReservationTableByReserveTitle(reservation.getReserveTitle());

        assertThat(reservation.getReserveState()).isEqualTo(ReservationState.COMPLETE);
    }

}