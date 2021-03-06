package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.books.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.factory.ReservationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.ecar.service.ECarReservationService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.factory.BookmarkFactory;
import com.ecar.servicestation.modules.user.factory.CarFactory;
import com.ecar.servicestation.modules.user.factory.HistoryFactory;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class UserMainApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    HistoryFactory historyFactory;

    @Autowired
    BookmarkFactory bookmarkFactory;

    @Autowired
    CarFactory carFactory;

    @Autowired
    ECarStationFactory eCarStationFactory;

    @Autowired
    ReservationFactory reservationFactory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    BookmarkRepository bookmarkRepository;

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

    private static final String BASE_URL_USER = "/user";

    private Station station;

    @BeforeEach
    void beforeEach() {
        withLoginAccount.init();

        this.station = eCarStationFactory.createStationAndAddCharger(1, 2);
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        stationRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("[?????? ?????? ?????? ??????]?????? ??????")
    public void find_histories_success() throws Exception {
        // Base
        historyFactory.createHistory(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER + "/histories")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[???????????? ??????]?????? ??????")
    public void save_bookmark_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        put(BASE_URL_USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNotNull();
    }

    @Test
    @DisplayName("[???????????? ?????? ??????]?????? ??????")
    public void find_bookmark_success() throws Exception {
        // Base
        bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER + "/bookmarks")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[???????????? ??????]?????? ??????")
    public void delete_bookmark_success() throws Exception {
        // Base
        bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(BASE_URL_USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNull();
    }

    @Test
    @DisplayName("[???????????? ??????]?????? - ???????????? ????????? ?????? ?????? ??????")
    public void delete_bookmark_failed_by_not_found() throws Exception {
        // Base
        bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(BASE_URL_USER + "/bookmark/-1")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1003));
    }

    @Test
    @DisplayName("[????????? ??????/?????? ?????? ??????]?????? ??????")
    public void find_user_reservation_statements_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Car car = registerCar(account);

        // Base(2)
        Charger charger = chargerRepository.findChargerByChargerNumber(2);
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        // Base(3)
        reservationFactory.confirmReservation(reservationFactory.createReservation(account, car.getId(), charger.getId(), start, end));

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER + "/reservation-statements")
                                .param("state", "0")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    private Car registerCar(Account account) {
        RegisterCarRequestDto registerCarRequest = new RegisterCarRequestDto();
        registerCarRequest.setCarModel("TEST MODEL2");
        registerCarRequest.setCarModelYear("2021");
        registerCarRequest.setCarType("??????");
        registerCarRequest.setCarNumber("99???9998");

        return carFactory.createCar(account, registerCarRequest);
    }

    private LocalDateTime getDataTimeAfter2HourFromNow(long chargerId) {
        ChargerTimeTableDto chargerTimeTable = eCarReservationService.getChargerTimeTable(chargerId, 0);
        //List<LocalDateTime> timeList = chargerTimeTable.getTimeTable().keySet().stream().sorted().collect(Collectors.toList());

        return LocalDateTime.now();
    }

}