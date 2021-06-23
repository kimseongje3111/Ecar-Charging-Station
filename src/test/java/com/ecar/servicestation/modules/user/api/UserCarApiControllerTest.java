package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.factory.CarFactory;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.PostConstruct;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@MockMvcTest
class UserCarApiControllerTest {

    private static final String USER_CAR = "/user/car";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CarFactory carFactory;

    @Autowired
    CarRepository carRepository;

    private RegisterCarRequestDto registerCarRequest;

    @PostConstruct
    void init() {
        this.registerCarRequest = new RegisterCarRequestDto();
        registerCarRequest.setCarModel("TEST MODEL");
        registerCarRequest.setCarModelYear("2021");
        registerCarRequest.setCarType("중형");
        registerCarRequest.setCarNumber("12T 3456");
    }

    @AfterEach
    void afterEach() {
        withLoginAccount.getAccount().getMyCars().clear();
        carRepository.deleteAll();
    }

    @Test
    @DisplayName("[사용자 차량 등록]정상 처리")
    public void save_user_car_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER_CAR + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerCarRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(carRepository.findAllByAccount(withLoginAccount.getAccount())).isNotEmpty();
    }

    @Test
    @DisplayName("[사용자 차량 조회]정상 처리")
    public void find_user_car_success() throws Exception {
        // Given
        Car car = carFactory.createCar(withLoginAccount.getAccount(), registerCarRequest);

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(USER_CAR).header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
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
    @DisplayName("[사용자 차량 삭제]정상 처리")
    public void delete_user_car_success() throws Exception {
        // Given
        Car car = carFactory.createCar(withLoginAccount.getAccount(), registerCarRequest);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(USER_CAR + "/" + car.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(carRepository.findAllByAccount(withLoginAccount.getAccount())).isEmpty();
    }
}