package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.factory.ReservationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.ecar.service.ECarReservationService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bookmark;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.domain.History;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.factory.BookmarkFactory;
import com.ecar.servicestation.modules.user.factory.CarFactory;
import com.ecar.servicestation.modules.user.factory.HistoryFactory;
import com.ecar.servicestation.modules.user.repository.BookmarkRepository;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class UserBasicApiControllerTest {

    private static final String USER = "/user";

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

    @AfterEach
    void afterEach() {
        withLoginAccount.getAccount().getHistories().clear();
        withLoginAccount.getAccount().getBookmarks().clear();
        withLoginAccount.getAccount().getMyCars().clear();

        carRepository.deleteAll();
        stationRepository.deleteAll();
        historyRepository.deleteAll();
        bookmarkRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("[회원 정보 조회]정상 처리")
    public void account_inquiry_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER).header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data").isNotEmpty());
    }

    @Test
    @DisplayName("[회원 정보 조회]실패 - 인증 헤더 및 토큰 미포함")
    public void account_inquiry_failed_by_not_token() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get(USER));

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[회원 정보 조회]실패 - 올바르지 않은 인증 토큰(잘못된 형식, 만료된 토큰)")
    public void account_inquiry_failed_by_invalid_token() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER).header("X-AUTH-TOKEN", "INVALID_TOKEN")
                );

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[최근 검색 목록 조회]정상 처리")
    public void find_histories_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger(1, 1);
        History history = historyFactory.createHistory(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER + "/history")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[즐겨찾기 등록]정상 처리")
    public void save_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger(1, 1);

        // When
        ResultActions perform =
                mockMvc.perform(
                        put(USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNotNull();
    }

    @Test
    @DisplayName("[즐겨찾기 목록 조회]정상 처리")
    public void find_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger(1, 1);
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER + "/bookmark")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[즐겨찾기 삭제]정상 처리")
    public void delete_bookmark_success() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger(1, 1);
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(USER + "/bookmark/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(bookmarkRepository.findBookmarkByAccountAndStation(withLoginAccount.getAccount(), station)).isNull();
    }

    @Test
    @DisplayName("[즐겨찾기 삭제]실패 - 즐겨찾기 목록에 없는 삭제 요청")
    public void delete_bookmark_failed_by_not_found() throws Exception {
        // Given
        Station station = eCarStationFactory.createStationAndAddCharger(1, 1);
        Bookmark bookmark = bookmarkFactory.createBookmark(withLoginAccount.getAccount(), station);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(USER + "/bookmark/-1")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1003));
    }

    @Test
    @DisplayName("[사용자 예약/충전 목록 조회]정상 처리")
    public void find_user_reservation_statements_success() throws Exception {
        // Given
        Account account = withLoginAccount.getAccount();
        Car car = registerCar(account);

        // Given(2)
        Station station = eCarStationFactory.createStationAndAddCharger(5, 5);
        Charger charger = chargerRepository.findChargerByChargerNumber(5);

        // Given(3)
        LocalDateTime start = getDataTimeAfter2HourFromNow(charger.getId());
        LocalDateTime end = start.plusHours(2);

        ReservationTable reservation = reservationFactory.createReservation(account, car.getId(), charger.getId(), start, end);
        reservation = reservationFactory.confirmReservation(reservation);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER + "/reservation-statements")
                                .param("state", "0")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    private Car registerCar(Account account) {
        RegisterCarRequestDto registerCarRequest = new RegisterCarRequestDto();
        registerCarRequest.setCarModel("TEST MODEL2");
        registerCarRequest.setCarModelYear("2021");
        registerCarRequest.setCarType("중형");
        registerCarRequest.setCarNumber("99수9998");

        return carFactory.createCar(account, registerCarRequest);
    }

    private LocalDateTime getDataTimeAfter2HourFromNow(long chargerId) {
        ChargerTimeTableDto chargerTimeTable = eCarReservationService.getChargerTimeTable(chargerId, 0);
        List<LocalDateTime> timeList = chargerTimeTable.getTimeTable().keySet().stream().sorted().collect(Collectors.toList());

        return timeList.get(0).plusHours(2);
    }
}