package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.infra.MockMvcTest;
import com.ecar.servicestation.infra.auth.WithLoginAccount;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Bank;
import com.ecar.servicestation.modules.user.dto.request.banks.CashInRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.CashOutRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.AuthBankRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.RegisterBankRequestDto;
import com.ecar.servicestation.modules.user.exception.banks.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.factory.BankFactory;
import com.ecar.servicestation.modules.user.factory.UserFactory;
import com.ecar.servicestation.modules.user.repository.BankRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@MockMvcTest
class UserBankApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithLoginAccount withLoginAccount;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserFactory userFactory;

    @Autowired
    BankFactory bankFactory;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BankRepository bankRepository;

    private static final String BASE_URL_USER_BANK = "/user/bank";

    @AfterEach
    void afterEach() {
        withLoginAccount.getAccount().getMyBanks().clear();

        bankRepository.deleteAll();
    }

    @Test
    @DisplayName("[사용자 계좌 등록]정상 처리")
    public void save_user_bank_account_success() throws Exception {
        // Given
        RegisterBankRequestDto registerBankRequest = new RegisterBankRequestDto();
        registerBankRequest.setBankName("농협");
        registerBankRequest.setBankAccountNumber("99999999999");
        registerBankRequest.setBankAccountOwner("ADMIN");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerBankRequest))
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
        List<Bank> myBanks = bankRepository.findAllByAccount(withLoginAccount.getAccount());

        assertThat(myBanks.size()).isEqualTo(1);
        assertThat(myBanks.get(0).isMainUsed()).isTrue();
        assertThat(myBanks.get(0).isBankAccountVerified()).isFalse();
    }

    @Test
    @DisplayName("[사용자 계좌 인증]정상 처리")
    public void confirm_user_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createBank("농협", "99999999999", account);

        // Given
        AuthBankRequestDto authBankRequest = new AuthBankRequestDto();
        authBankRequest.setBankId(bank.getId());
        authBankRequest.setPaymentPassword("12341234");
        authBankRequest.setAuthMsg(bank.getBankAccountAuthMsg());
        authBankRequest.setCertificateId(1L);
        authBankRequest.setCertificatePassword("123456789");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authBankRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        List<Bank> myBanks = bankRepository.findAllByAccount(account);

        assertThat(myBanks.size()).isEqualTo(1);
        assertThat(myBanks.get(0).isMainUsed()).isTrue();
        assertThat(myBanks.get(0).isBankAccountVerified()).isTrue();
        assertThat(myBanks.get(0).getBankAccountAccessToken()).isNotEmpty();
    }

    @Test
    @DisplayName("[사용자 계좌 인증]실패 - 인증 메시지 불일치")
    public void confirm_user_bank_account_failed_by_auth_message_mismatched() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createBank("농협", "99999999999", account);

        // Given
        AuthBankRequestDto authBankRequest = new AuthBankRequestDto();
        authBankRequest.setBankId(bank.getId());
        authBankRequest.setPaymentPassword("12341234");
        authBankRequest.setAuthMsg("INVALID_AUTH_MESSAGE");
        authBankRequest.setCertificateId(1L);
        authBankRequest.setCertificatePassword("123456789");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authBankRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1010));
    }

    @Test
    @DisplayName("[사용자 계좌 목록 조회]정상 처리")
    public void find_user_bank_account_list_success() throws Exception {
        // Base
        bankFactory.createVerifiedBank("농협", "99999999999", withLoginAccount.getAccount());

        // When
        ResultActions perform =
                mockMvc.perform(
                        get(BASE_URL_USER_BANK).header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
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
    @DisplayName("[사용자 계좌 삭제]정상 처리")
    public void delete_user_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createVerifiedBank("농협", "99999999999", account);

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(BASE_URL_USER_BANK + "/" + bank.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        List<Bank> myBanks = bankRepository.findAllByAccount(account);

        assertThat(myBanks.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[사용자 계좌 삭제]실패 - 인증 계좌 미등록")
    public void delete_user_bank_account_failed_by_not_found() throws Exception {
        // Base
        Bank bank = bankFactory.createBank("농협", "99999999999", withLoginAccount.getAccount());

        // When
        ResultActions perform =
                mockMvc.perform(
                        delete(BASE_URL_USER_BANK + "/" + bank.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1009));
    }

    @Test
    @DisplayName("[주사용 계좌 변경]정상 처리")
    public void change_user_main_used_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank1 = bankFactory.createVerifiedBank("농협", "99999999999", account);
        Bank bank2 = bankFactory.createVerifiedBank("신한", "11111111111", account);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/main-used/" + bank2.getId())
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(bankRepository.findById(bank1.getId()).orElseThrow(CBankNotFoundException::new).isMainUsed()).isFalse();
        assertThat(bankRepository.findById(bank2.getId()).orElseThrow(CBankNotFoundException::new).isMainUsed()).isTrue();
    }

    @Test
    @DisplayName("[현금(캐쉬) 충전]정상 처리")
    public void charge_cash_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("농협", "99999999999", account);

        // Given
        CashInRequestDto cashInRequest = new CashInRequestDto();
        cashInRequest.setAmount(10000);
        cashInRequest.setPaymentPassword("12341234");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/cash-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cashInRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new).getCash()).isEqualTo(10000);
    }

    @Test
    @DisplayName("[현금(캐쉬) 충전]실패 - 결제 비밀번호 불일치")
    public void charge_cash_failed_by_payment_password_mismatched() throws Exception {
        // Base
        bankFactory.createVerifiedBank("농협", "99999999999", withLoginAccount.getAccount());

        // Given
        CashInRequestDto cashInRequest = new CashInRequestDto();
        cashInRequest.setAmount(10000);
        cashInRequest.setPaymentPassword("1234");

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/cash-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cashInRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1011));
    }

    @Test
    @DisplayName("[현금(캐쉬) 환불]정상 처리")
    public void refund_cash_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("농협", "99999999999", account);
        userFactory.cashInit(account, 50000);

        // Given
        CashOutRequestDto cashOutRequest = new CashOutRequestDto();
        cashOutRequest.setAmount(10000);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/cash-out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cashOutRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("success").value(true))
                .andExpect(jsonPath("responseCode").value(0))
                .andExpect(jsonPath("message").value("성공하였습니다."));

        // Then(2)
        assertThat(userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new).getCash()).isEqualTo(40000);
    }

    @Test
    @DisplayName("[현금(캐쉬) 환불]실패 - 보유 현금을 초과한 환불 요청")
    public void refund_cash_failed_by_user_cash_not_enough() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("농협", "99999999999", account);
        userFactory.cashInit(account, 50000);

        // Given
        CashOutRequestDto cashOutRequest = new CashOutRequestDto();
        cashOutRequest.setAmount(60000);

        // When
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_URL_USER_BANK + "/cash-out")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cashOutRequest))
                                .header("X-AUTH-TOKEN", withLoginAccount.getAuthToken())
                );

        // Then
        perform
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("success").value(false))
                .andExpect(jsonPath("responseCode").value(-1011));
    }

}