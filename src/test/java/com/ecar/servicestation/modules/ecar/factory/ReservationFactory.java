package com.ecar.servicestation.modules.ecar.factory;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.ReservationTable;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.repository.ChargerRepository;
import com.ecar.servicestation.modules.ecar.repository.ReservationRepository;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.exception.cars.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.users.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationFactory {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ChargerRepository chargerRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationTable createReservation(Account account, long carId, long chargerId, LocalDateTime start, LocalDateTime end) {
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);
        Car car = carRepository.findById(carId).orElseThrow(CCarNotFoundException::new);
        Charger charger = chargerRepository.findById(chargerId).orElseThrow(CChargerNotFoundException::new);

        ReservationTable reservation =
                reservationRepository.save(
                        ReservationTable.builder()
                                .reservedAt(LocalDateTime.now())
                                .chargeStartDateTime(start)
                                .chargeEndDateTime(end)
                                .isSentNotificationOfReservationStart(false)
                                .isSentNotificationOfChargingEnd(false)
                                .account(account)
                                .car(car)
                                .charger(charger)
                                .build()
                );

        reservation.applyReservation(150 * 7);

        return reservation;
    }

    @Transactional
    public ReservationTable confirmReservation(ReservationTable reservation) {
        reservation.confirmReservation(0);

        return reservation;
    }

    @Transactional
    public ReservationTable chargingReservation(ReservationTable reservation) {
        if (reservation.startCharging(30)) {
            return reservation;
        }

        return null;
    }

}
