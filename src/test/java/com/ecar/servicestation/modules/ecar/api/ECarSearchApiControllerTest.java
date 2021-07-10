package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.dto.request.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.SearchLocationDto;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.PostConstruct;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class ECarSearchApiControllerTest {

    private static final String E_CAR = "/ecar";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    StationRepository stationRepository;

    private SearchConditionDto condition;
    private SearchLocationDto location;

    @PostConstruct
    void init() {
        this.condition = new SearchConditionDto();
        condition.setSearch("대전 서구 둔산동");
        condition.setChargerTp(2);  // 급속
        condition.setLatitude(Double.valueOf("36.357692"));
        condition.setLongitude(Double.valueOf("127.381050"));

        this.location = new SearchLocationDto();
        condition.setChargerTp(2);
        location.setLatitude(Double.valueOf("36.357692"));
        location.setLongitude(Double.valueOf("127.381050"));
    }

    @AfterEach
    void afterEach() {
        stationRepository.deleteAll();
    }

    @Test
    @DisplayName("[전기차 충전소 조회]정상 처리 - 주소")
    public void search_ecar_charging_station_with_address_and_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .param("search", condition.getSearch())
                                .param("latitude", String.valueOf(condition.getLatitude()))
                                .param("longitude", String.valueOf(condition.getLongitude()))
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
    @DisplayName("[전기차 충전소 조회]정상 처리 - 검색 조건을 포함한 주소")
    public void search_ecar_charging_station_with_address_and_condition_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(E_CAR + "/find")
                                .param("search", condition.getSearch())
                                .param("latitude", String.valueOf(condition.getLatitude()))
                                .param("longitude", String.valueOf(condition.getLongitude()))
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
    @DisplayName("[전기차 충전소 조회]정상 처리 - 현위치(위도/경도)")
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
                                .param("search", "데이터 없음")
                                .param("latitude", String.valueOf(condition.getLatitude()))
                                .param("longitude", String.valueOf(condition.getLongitude()))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-3000));
    }
}