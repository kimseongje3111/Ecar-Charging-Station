package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.mail.EmailMessage;
import com.ecar.servicestation.infra.mail.MailService;
import com.ecar.servicestation.modules.user.factory.UserFactory;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.LoginRequestDto;
import com.ecar.servicestation.modules.user.dto.SignUpRequestDto;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import com.ecar.servicestation.modules.user.service.UserLoginAndSignUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.NestedServletException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class UserLoginAndSignUpApiControllerTest {

    private static final String USER = "/user";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserLoginAndSignUpService userLoginAndSignUpService;

    @Autowired
    UserFactory userFactory;

    @MockBean
    MailService mailService;

    @Test
    @DisplayName("[회원가입]정상 처리")
    public void signUp_success() throws Exception {
        // Given
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUserName("admin");
        signUpRequest.setPassword("1234");
        signUpRequest.setEmail("admin@test.com");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/sign-up")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(signUpRequest))
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        Optional<Account> account = userRepository.findAccountByEmail(signUpRequest.getEmail());
        assertThat(account).isNotEmpty();
        assertThat(account.get().getPassword()).isNotEqualTo(signUpRequest.getPassword());
        assertThat(account.get().getEmailAuthToken()).isNotNull();

        then(mailService).should().send(any(EmailMessage.class));
    }

    @Test
    @DisplayName("[회원가입]실패 - 필수 요청 파라미터 부재")
    public void signUp_failed_by_missing_required_request_parameters() throws Exception {
        // Given
        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setPassword("1234");
        signUpRequest.setEmail("admin@test.com");

        // When
        // Then
        assertThrows(NestedServletException.class, () -> mockMvc.perform(
                post(USER + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest))
        ));
    }

    @Test
    @DisplayName("[회원가입]실패 - 이미 존재하는 계정(이메일 중복)")
    public void signUp_failed_by_duplicated_email() throws Exception {
        // Given
        userFactory.createSimpleAccount("admin", "1234", "admin@test.com");

        SignUpRequestDto signUpRequest = new SignUpRequestDto();
        signUpRequest.setUserName("admin2");
        signUpRequest.setPassword("1234");
        signUpRequest.setEmail("admin@test.com");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/sign-up")
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
        // Given
        Account newAccount = userFactory.createSimpleAccount("admin", "1234", "admin@test.com");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/email-auth-token")
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

        Optional<Account> account = userRepository.findAccountByEmail(newAccount.getEmail());
        assertThat(account.get().isEmailAuthVerified()).isTrue();
        assertThat(account.get().getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("[로그인]정상 처리 - 로그인 정보 일치, 이메일 인증이 완료된 계정")
    public void login_success() throws Exception {
        // Given
        userFactory.createVerifiedAccount("admin", "1234", "admin@test.com");

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("admin@test.com");
        loginRequestDto.setPassword("1234");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequestDto))
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
        // Given
        userFactory.createVerifiedAccount("admin", "1234", "admin@test.com");

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("user@test.com");
        loginRequestDto.setPassword("1234");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequestDto))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1000));
    }

    @Test
    @DisplayName("[로그인]실패 - 로그인 정보 불일치")
    public void login_failed_by_login_info_mismatch() throws Exception {
        // Given
        userFactory.createVerifiedAccount("admin", "1234", "admin@test.com");

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("admin@test.com");
        loginRequestDto.setPassword("4321");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(USER + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequestDto))
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1001));
    }
}