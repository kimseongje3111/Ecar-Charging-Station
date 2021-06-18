package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ECarBasicApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    ECarStationFactory eCarStationFactory;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    StationRepository stationRepository;

    private final String E_CAR = "/ecar";

    private Station station;

    @BeforeEach
    void beforeEach() {
        this.station = eCarStationFactory.createStationAndAddCharger();
    }

    @AfterEach
    void afterEach() {
        stationRepository.deleteAll();
    }

    @Test
    @DisplayName("[전기차 충전소 단건 조회]정상 처리")
    public void find_station_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/station/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
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
    @DisplayName("[전기차 충전소 단건 조회 및 기록 저장]정상 처리")
    public void find_station_and_save_history_success() throws Exception {
        // Given
        Account account = withLoginAccount.getAccount();

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/station/" + station.getId() + "/register")
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
        assertThat(historyRepository.existsHistoryByAccountAndStation(account, station)).isTrue();
    }

    @Test
    @DisplayName("[전기차 충전소의 충전기 단건 조회]정상 처리")
    public void find_charger_success() throws Exception {
        // Given
        List<Charger> chargers = new ArrayList<>(station.getChargers());

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/charger/" + chargers.get(0).getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."))
                .andExpect(jsonPath("data").isNotEmpty());
    }
}