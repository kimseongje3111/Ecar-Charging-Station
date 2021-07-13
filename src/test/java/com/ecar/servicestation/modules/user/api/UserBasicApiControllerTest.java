package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UserBasicApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    private static final String BASE_URL_USER = "/user";

    @Test
    @DisplayName("[회원 정보 조회]정상 처리")
    public void account_inquiry_success() throws Exception {
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
    @DisplayName("[회원 정보 조회]실패 - 인증 헤더 및 토큰 미포함")
    public void account_inquiry_failed_by_not_token() throws Exception {
        // When
        ResultActions perform = mockMvc.perform(get(BASE_URL_USER));

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
                        get(BASE_URL_USER).header("X-AUTH-TOKEN", "INVALID_TOKEN")
                );

        // Then
        perform
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/exception/entry-point"));
    }


}