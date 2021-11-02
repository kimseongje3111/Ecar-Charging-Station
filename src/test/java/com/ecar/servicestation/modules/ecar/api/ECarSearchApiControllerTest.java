package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchLocationDto;
import com.ecar.servicestation.modules.ecar.repository.StationRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StationRepository stationRepository;

    private static final String BASE_URL_ECAR_SEARCH = "/ecar/find";

    private SearchConditionDto searchCondition;
    private SearchLocationDto searchLocation;

    @BeforeEach
    void beforeEach() {
        withLoginAccount.init();

        this.searchCondition = new SearchConditionDto();
        searchCondition.setSearch("일산");
        searchCondition.setChargerTp(2);  // 급속
        searchCondition.setLatitude(Double.valueOf("36.357692"));
        searchCondition.setLongitude(Double.valueOf("127.381050"));

        this.searchLocation = new SearchLocationDto();
        searchCondition.setChargerTp(2);
        searchLocation.setLatitude(Double.valueOf("36.357692"));
        searchLocation.setLongitude(Double.valueOf("127.381050"));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Test
    @DisplayName("[전기차 충전소 조회]정상 처리 - 주소")
    public void search_ecar_charging_station_with_address_and_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_ECAR_SEARCH)
                                .param("search", searchCondition.getSearch())
                                .param("latitude", String.valueOf(searchCondition.getLatitude()))
                                .param("longitude", String.valueOf(searchCondition.getLongitude()))
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
                        get(BASE_URL_ECAR_SEARCH)
                                .param("search", searchCondition.getSearch())
                                .param("latitude", String.valueOf(searchCondition.getLatitude()))
                                .param("longitude", String.valueOf(searchCondition.getLongitude()))
                                .param("chargerTp", String.valueOf(searchCondition.getChargerTp()))
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
                        get(BASE_URL_ECAR_SEARCH + "/location")
                                .param("latitude", String.valueOf(searchLocation.getLatitude()))
                                .param("longitude", String.valueOf(searchLocation.getLongitude()))
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
                        get(BASE_URL_ECAR_SEARCH)
                                .param("search", "데이터 없음")
                                .param("latitude", String.valueOf(searchCondition.getLatitude()))
                                .param("longitude", String.valueOf(searchCondition.getLongitude()))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-3000));
    }

}