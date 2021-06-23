package com.ecar.servicestation.modules.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.persistence.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Bank {

    @Id
    @GeneratedValue
    @Column(name = "bank_id")
    private Long id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false, unique = true)
    private String bankAccountNumber;

    @Column(nullable = false)
    private String bankAccountOwner;

    private String paymentPassword;

    private String bankAccountAuthMsg;

    private String bankAccountAccessToken;

    private boolean bankAccountVerified;

    private boolean mainUsed;

    private LocalDateTime registeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // 비지니스 메서드 //

    public String generateAuthMsg() {
        try {
            Resource resource = new ClassPathResource("bank_msg.csv");
            List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);

            int ranIdx1 = (int) (Math.random() * lines.size());
            int ranIdx2 = (int) (Math.random() * lines.size());
            this.bankAccountAuthMsg = lines.get(ranIdx1).split(",")[0] + " " + lines.get(ranIdx2).split(",")[1];

            return this.bankAccountAuthMsg;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void successBankAccountAuthentication() {
        this.bankAccountVerified = true;
        this.registeredAt = LocalDateTime.now();
    }

    public void setPaymentPasswordAndAccessToken(String password, String token) {
        this.paymentPassword = password;
        this.bankAccountAccessToken = token;
    }
}
