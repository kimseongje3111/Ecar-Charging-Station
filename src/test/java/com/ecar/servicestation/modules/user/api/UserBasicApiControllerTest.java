package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateNotificationRequest;
import com.ecar.servicestation.modules.user.dto.request.users.UpdatePasswordRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateUserRequestDto;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UserBasicApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String BASE_URL_USER = "/user";

    @BeforeEach
    void beforeEach() {
        withLoginAccount.init();
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[사용자 정보 조회]정상 처리")
    public void inquiry_user_info_success() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER).header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
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
    @DisplayName("[사용자 정보 조회]실패 - 인증 헤더 및 토큰 미포함")
    public void inquiry_user_info_failed_by_not_token() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get(BASE_URL_USER));

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[사용자 정보 조회]실패 - 올바르지 않은 인증 토큰(잘못된 형식, 만료된 토큰)")
    public void inquiry_user_info_failed_by_invalid_token() throws Exception {
        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER).header("X-AUTH-TOKEN", "INVALID-TOKEN")
                );

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }

    @Test
    @DisplayName("[사용자 정보 변경]정상 처리")
    public void update_user_info_success() throws Exception {
        // Given
        UpdateUserRequestDto updateUserRequest = new UpdateUserRequestDto();
        updateUserRequest.setUserName("ADMIN02");
        updateUserRequest.setPhoneNumber("ADMIN02-PHONE-NUMBER");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateUserRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        Account account = userRepository.findAccountByEmail(withLoginAccount.getAccount().getEmail()).orElseThrow(CUserNotFoundException::new);

        assertThat(account.getPhoneNumber()).isEqualTo("ADMIN02-PHONE-NUMBER");
    }

    @Test
    @DisplayName("[사용자 비밀번호 변경]정상 처리")
    public void update_user_password_success() throws Exception {
        // Given
        UpdatePasswordRequestDto updatePasswordRequest = new UpdatePasswordRequestDto();
        updatePasswordRequest.setCurrentPassword("ADMIN01-PASSWORD");
        updatePasswordRequest.setNewPassword("ADMIN01-NEW-PASSWORD");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER + "/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatePasswordRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        Account account = userRepository.findAccountByEmail(withLoginAccount.getAccount().getEmail()).orElseThrow(CUserNotFoundException::new);

        assertThat(passwordEncoder.matches(updatePasswordRequest.getNewPassword(), account.getPassword())).isTrue();
    }

    @Test
    @DisplayName("[사용자 알림 설정 변경]정상 처리")
    public void update_user_notification_success() throws Exception {
        // Given
        UpdateNotificationRequest updateNotificationRequest = new UpdateNotificationRequest();
        updateNotificationRequest.setOnNotificationOfReservationStart(true);
        updateNotificationRequest.setMinutesBeforeReservationStart(30);
        updateNotificationRequest.setOnNotificationOfChargingEnd(false);
        updateNotificationRequest.setMinutesBeforeChargingEnd(30);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER + "/notification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateNotificationRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        Account account = userRepository.findAccountByEmail(withLoginAccount.getAccount().getEmail()).orElseThrow(CUserNotFoundException::new);

        assertThat(account.isOnNotificationOfChargingEnd()).isFalse();
    }

}