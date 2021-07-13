package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.factory.UserFactory;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.users.LoginRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.SignUpRequestDto;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class UserLoginAndSignUpApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserFactory userFactory;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[회원가입]정상 처리")
    public void signUp_success() throws Exception {
        // Given
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUserName("USER01");
        signUpRequest.setPassword("USER01-PASSWORD");
        signUpRequest.setEmail("USER01@test.com");
        signUpRequest.setPhoneNumber("USER01-PHONE-NUMBER");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest))
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        Optional<Account> account = userRepository.findAccountByEmail(signUpRequest.getEmail());

        assertThat(account.orElseThrow(CUserNotFoundException::new).getPassword()).isNotEqualTo(signUpRequest.getPassword());
        assertThat(account.orElseThrow(CUserNotFoundException::new).getEmailAuthToken()).isNotNull();
    }

    @Test
    @DisplayName("[회원가입]실패 - 이미 존재하는 계정(이메일 중복)")
    public void signUp_failed_by_duplicated_email() throws Exception {
        // Base
        userFactory.createSimpleAccount("USER01", "USER01-PASSWORD", "USER01@test.com");

        // Given
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUserName("USER02");
        signUpRequest.setPassword("USER02-PASSWORD");
        signUpRequest.setEmail("USER01@test.com");
        signUpRequest.setPhoneNumber("USER02-PHONE-NUMBER");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1002));
    }

    @Test
    @DisplayName("[회원가입 후 이메일 계정 인증]정상 처리")
    public void account_email_authentication_after_signUp_success() throws Exception {
        // Base
        Account newAccount = userFactory.createSimpleAccount("USER01", "USER01-PASSWORD", "USER01@test.com");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/email-auth-token")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .param("email", newAccount.getEmail())
                                .param("token", newAccount.getEmailAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        Optional<Account> account = userRepository.findAccountByEmail(newAccount.getEmail());

        assertThat(account.orElseThrow(CUserNotFoundException::new).isEmailAuthVerified()).isTrue();
        assertThat(account.orElseThrow(CUserNotFoundException::new).getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("[로그인]정상 처리 - 로그인 정보 일치, 이메일 인증이 완료된 계정")
    public void login_success() throws Exception {
        // Base
        userFactory.createVerifiedAccount("USER01", "USER01-PASSWORD", "USER01@test.com");

        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("USER01@test.com");
        loginRequest.setPassword("USER01-PASSWORD");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
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
    @DisplayName("[로그인]실패 - 존재하지 않은 계정")
    public void login_failed_by_user_not_found() throws Exception {
        // Base
        userFactory.createVerifiedAccount("USER01", "USER01-PASSWORD", "USER01@test.com");

        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("USER02@test.com");
        loginRequest.setPassword("USER02-PASSWORD");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1000));
    }

    @Test
    @DisplayName("[로그인]실패 - 로그인 정보 불일치")
    public void login_failed_by_login_info_mismatched() throws Exception {
        // Base
        userFactory.createVerifiedAccount("USER01", "USER01-PASSWORD", "USER01@test.com");

        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("USER01@test.com");
        loginRequest.setPassword("USER01-INVALID-PASSWORD");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1001));
    }

}