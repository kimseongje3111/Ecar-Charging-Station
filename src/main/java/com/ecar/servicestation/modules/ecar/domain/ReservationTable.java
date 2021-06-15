package com.ecar.servicestation.modules.ecar.domain;

import com.ecar.servicestation.modules.user.domain.Account;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ReservationTable {

    @Id
    @GeneratedValue
    @Column(name = "reservation_id")
    private Long id;

    @Column(unique = true)
    private String reserveTitle;

    private LocalDateTime reservedAt;

    private LocalDateTime chargeStartDateTime;

    private LocalDateTime chargeEndDateTime;

    private Integer reserveFares;

    @Enumerated(EnumType.STRING)
    private ReservationState reserveState;

    private LocalDateTime stateLastChangedAt;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private Integer usedPoint;

    private boolean isNotificationSet;

    private LocalDateTime notificationScheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id")
    private Charger charger;

    // 비지니스 메서드 //

    public void applyReservation(int faresByChargerType) {
        // 요금 계산
        Duration duration = Duration.between(chargeStartDateTime, chargeEndDateTime);
        BigDecimal seconds = new BigDecimal(String.valueOf(duration.getSeconds()));
        BigDecimal secondsPerHour = new BigDecimal("3600");
        BigDecimal faresPerHour = new BigDecimal(String.valueOf(faresByChargerType));

        this.reserveFares = seconds.divide(secondsPerHour).multiply(faresPerHour).intValue();
        this.reserveState = ReservationState.STAND_BY;
        this.stateLastChangedAt = LocalDateTime.now();
    }

    public void confirmReservation() {
        this.reserveState = ReservationState.PAYMENT;
        this.stateLastChangedAt = LocalDateTime.now();
    }
}
