package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequest;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CarFactory {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Transactional
    public Car createCar(Account account, RegisterCarRequest request) {
        account = userRepository.findAccountByEmail(account.getEmail()).orElseThrow(CUserNotFoundException::new);

        Car car =
                carRepository.save(
                        Car.builder()
                                .carModel(request.getCarModel())
                                .carModelYear(request.getCarModelYear())
                                .carType(request.getCarType())
                                .carNumber(request.getCarNumber())
                                .account(account)
                                .build()
                );

        account.addCar(car);

        return car;
    }
}
