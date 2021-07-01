package com.ecar.servicestation.modules.ecar.domain;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private Integer usedCashPoint;

    private boolean isNotificationSet;

    private LocalDateTime notificationScheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

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

    public void confirmReservation(int usedCashPoint) {
        this.paymentType = usedCashPoint > 0 ? PaymentType.CASH_AND_POINT : PaymentType.CASH;
        this.usedCashPoint = usedCashPoint;
        this.reserveState = ReservationState.PAYMENT;
        this.stateLastChangedAt = LocalDateTime.now();
        this.reserveTitle = makeReserveTitle();
    }

    public void cancelReservation() {
        this.reserveState = ReservationState.CANCEL;
        this.stateLastChangedAt = LocalDateTime.now();
    }

    public void setOnNotification(int minutes) {
        this.isNotificationSet = true;
        this.notificationScheduledAt = this.chargeEndDateTime.minusMinutes(minutes);
    }

    public void setOffNotification() {
        this.isNotificationSet = false;
    }

    private String makeReserveTitle() {
        StringBuilder stringBuilder = new StringBuilder();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm");

        return stringBuilder
                .append("CH#").append(this.charger.getId())
                .append("S#").append(this.chargeStartDateTime.format(dateTimeFormatter))
                .append("E#").append(this.chargeEndDateTime.format(dateTimeFormatter))
                .append("CN#").append(this.car.get4DigitsOfCarNumber())
                .toString();
    }
}
