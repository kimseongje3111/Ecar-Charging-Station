package com.ecar.servicestation.modules.user.factory;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.RegisterCarRequest;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CarFactory {

    private final CarRepository carRepository;

    @Transactional
    public Car createCar(Account account, RegisterCarRequest request) {
        return carRepository.save(
                Car.builder()
                        .carModel(request.getCarModel())
                        .carModelYear(request.getCarModelYear())
                        .carType(request.getCarType())
                        .carNumber(request.getCarNumber())
                        .account(account)
                        .build()
        );
    }
}
