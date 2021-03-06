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
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void beforeEach() {
        withLoginAccount.init();
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[????????? ?????? ??????]?????? ??????")
    public void save_user_bank_account_success() throws Exception {
        // Given
        RegisterBankRequestDto registerBankRequest = new RegisterBankRequestDto();
        registerBankRequest.setBankName("BANK01");
        registerBankRequest.setBankAccountNumber("BANK01-ACCOUNT-NUMBER");
        registerBankRequest.setBankAccountOwner("ADMIN01");
        registerBankRequest.setCertificateId(1L);
        registerBankRequest.setCertificatePassword("ADMIN01-CERTIFICATE-PASSWORD");

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
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("data").isNotEmpty());

        // Then(2)
        List<Bank> myBanks = bankRepository.findAllByAccount(withLoginAccount.getAccount());

        assertThat(myBanks.size()).isEqualTo(1);
        assertThat(myBanks.get(0).isMainUsed()).isTrue();
        assertThat(myBanks.get(0).getBankAccountAccessToken()).isNotEmpty();
        assertThat(myBanks.get(0).isBankAccountVerified()).isFalse();
    }

    @Test
    @DisplayName("[????????? ?????? ??????]?????? ??????")
    public void confirm_user_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);

        // Given
        AuthBankRequestDto authBankRequest = new AuthBankRequestDto();
        authBankRequest.setBankId(bank.getId());
        authBankRequest.setPaymentPassword("BANK01-PAYMENT-PASSWORD");
        authBankRequest.setAuthMsg(bank.getBankAccountAuthMsg());

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
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        List<Bank> myBanks = bankRepository.findAllByAccount(account);

        assertThat(myBanks.size()).isEqualTo(1);
        assertThat(myBanks.get(0).isMainUsed()).isTrue();
        assertThat(myBanks.get(0).isBankAccountVerified()).isTrue();
    }

    @Test
    @DisplayName("[????????? ?????? ??????]?????? - ?????? ????????? ?????????")
    public void confirm_user_bank_account_failed_by_auth_message_mismatched() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);

        // Given
        AuthBankRequestDto authBankRequest = new AuthBankRequestDto();
        authBankRequest.setBankId(bank.getId());
        authBankRequest.setPaymentPassword("BANK01-PAYMENT-PASSWORD");
        authBankRequest.setAuthMsg("INVALID-AUTH-MESSAGE");

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
    @DisplayName("[????????? ?????? ?????? ??????]?????? ??????")
    public void find_user_bank_account_list_success() throws Exception {
        // Base
        bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", withLoginAccount.getAccount());

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
                .andExpect(jsonPath("message").value("?????????????????????."))
                .andExpect(jsonPath("dataList").isNotEmpty());
    }

    @Test
    @DisplayName("[????????? ?????? ??????]?????? ??????")
    public void delete_user_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank = bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);

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
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        List<Bank> myBanks = bankRepository.findAllByAccount(account);

        assertThat(myBanks.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("[????????? ?????? ??????]?????? - ?????? ?????? ?????????")
    public void delete_user_bank_account_failed_by_not_found() throws Exception {
        // Base
        Bank bank = bankFactory.createBank("BANK01", "BANK01-ACCOUNT-NUMBER", withLoginAccount.getAccount());

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
    @DisplayName("[????????? ?????? ??????]?????? ??????")
    public void change_user_main_used_bank_account_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        Bank bank1 = bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);
        Bank bank2 = bankFactory.createVerifiedBank("BANK02", "BANK02-ACCOUNT-NUMBER", account);

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
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        assertThat(bankRepository.findById(bank1.getId()).orElseThrow(CBankNotFoundException::new).isMainUsed()).isFalse();
        assertThat(bankRepository.findById(bank2.getId()).orElseThrow(CBankNotFoundException::new).isMainUsed()).isTrue();
    }

    @Test
    @DisplayName("[??????(??????) ??????]?????? ??????")
    public void charge_cash_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);

        // Given
        CashInRequestDto cashInRequest = new CashInRequestDto();
        cashInRequest.setAmount(10000);
        cashInRequest.setPaymentPassword("BANK01-PAYMENT-PASSWORD");

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
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        assertThat(userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new).getCash()).isEqualTo(10000);
    }

    @Test
    @DisplayName("[??????(??????) ??????]?????? - ?????? ???????????? ?????????")
    public void charge_cash_failed_by_payment_password_mismatched() throws Exception {
        // Base
        bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", withLoginAccount.getAccount());

        // Given
        CashInRequestDto cashInRequest = new CashInRequestDto();
        cashInRequest.setAmount(10000);
        cashInRequest.setPaymentPassword("INVALID-PAYMENT-PASSWORD");

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
    @DisplayName("[??????(??????) ??????]?????? ??????")
    public void refund_cash_success() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);
        userFactory.cashInit(account, 50000);

        // Given
        CashOutRequestDto cashOutRequest = new CashOutRequestDto();
        cashOutRequest.setAmount(10000);
        cashOutRequest.setPaymentPassword("BANK01-PAYMENT-PASSWORD");

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
                .andExpect(jsonPath("message").value("?????????????????????."));

        // Then(2)
        assertThat(userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new).getCash()).isEqualTo(40000);
    }

    @Test
    @DisplayName("[??????(??????) ??????]?????? - ?????? ????????? ????????? ?????? ??????")
    public void refund_cash_failed_by_user_cash_not_enough() throws Exception {
        // Base
        Account account = withLoginAccount.getAccount();
        bankFactory.createVerifiedBank("BANK01", "BANK01-ACCOUNT-NUMBER", account);
        userFactory.cashInit(account, 50000);

        // Given
        CashOutRequestDto cashOutRequest = new CashOutRequestDto();
        cashOutRequest.setAmount(60000);
        cashOutRequest.setPaymentPassword("BANK01-PAYMENT-PASSWORD");

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