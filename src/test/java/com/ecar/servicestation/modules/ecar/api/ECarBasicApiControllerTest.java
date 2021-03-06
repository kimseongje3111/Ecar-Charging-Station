package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import com.ecar.servicestation.modules.ecar.factory.ECarStationFactory;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.repository.HistoryRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
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
    UserRepository userRepository;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    ChargerRepository chargerRepository;

    private static final String BASE_URL_ECAR = "/ecar";

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
    }

    @Test
    @DisplayName("[????????? ????????? ?????? ??????]?????? ??????")
    public void find_station_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_ECAR + "/station/" + station.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("data").isNotEmpty());
    }

    @Test
    @DisplayName("[????????? ????????? ?????? ?????? ??? ?????? ??????]?????? ??????")
    public void find_station_and_save_history_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_ECAR + "/station/" + station.getId() + "/record")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("data").isNotEmpty());

        // Then(2)
        assertThat(historyRepository.existsHistoryByAccountAndStation(withLoginAccount.getAccount(), station)).isTrue();
    }

    @Test
    @DisplayName("[????????? ???????????? ????????? ?????? ??????]?????? ??????")
    public void find_charger_success() throws Exception {
        // Base
        Charger charger = chargerRepository.findChargerByChargerNumber(2);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_ECAR + "/charger/" + charger.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("data").isNotEmpty());
    }

}