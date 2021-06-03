package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.infra.data.ECarChargingStationInfoProvider;
import com.ecar.servicestation.infra.map.MapService;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.ecar.servicestation.modules.ecar.dto.SearchLocation;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ECarSearchApiControllerTest {

    private final String E_CAR = "/ecar";

    private SearchCondition condition;
    private SearchLocation location;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    ChargerRepository chargerRepository;

    @Autowired
    ECarChargingStationInfoProvider eCarChargingStationInfoProvider;

    @Autowired
    MapService mapService;

    @BeforeEach
    void beforeEach() {
        this.condition = new SearchCondition();
        condition.setSearch("대전 서구");
        condition.setCpStat(1);     // 충전 가능
        condition.setChargerTp(2);  // 급속

        this.location = new SearchLocation();
        location.setLatitude(Double.valueOf("36.357692"));
        location.setLongitude(Double.valueOf("127.381050"));
    }

    @Test
    @DisplayName("[전기차 충전소 조회]정상 처리 - 주소")
    public void search_ecar_charging_station_with_address_and_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .param("search", condition.getSearch())
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
    @DisplayName("[전기차 충전소 조회]정상 처리 - 충전소명")
    public void search_ecar_charging_station_with_station_name_and_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .param("searchType", "1")
                                .param("search", "시청")
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
    @DisplayName("[전기차 충전소 조회]정상 처리 - 주소 및 검색 조건")
    public void search_ecar_charging_station_with_address_and_condition_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .param("search", condition.getSearch())
                                .param("cpStat", String.valueOf(condition.getCpStat()))
                                .param("chargerTp", String.valueOf(condition.getChargerTp()))
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
    @DisplayName("[전기차 충전소 조회]정상 처리 - 위도/경도")
    public void search_ecar_charging_station_with_latitude_and_longitude_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find/location")
                                .param("latitude", String.valueOf(location.getLatitude()))
                                .param("longitude", String.valueOf(location.getLongitude()))
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
    @DisplayName("[전기차 충전소 조회]실패 - 결과 데이터를 찾을 수 없음")
    public void search_ecar_charging_station_failed_by_miss_required_parameters() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-3000));
    }
}