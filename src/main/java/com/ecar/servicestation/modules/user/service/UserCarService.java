package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.RegisterCarRequest;
import com.ecar.servicestation.modules.user.exception.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.repository.CarRepository;
import com.ecar.servicestation.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCarService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ModelMapper modelMapper;

    public Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }

    @Transactional
    public void saveCar(RegisterCarRequest request) {
        Account account = getUserBasicInfo();
        Car car = modelMapper.map(request, Car.class);

        account.addCar(car);
        carRepository.save(car);
    }

    public List<Car> getMyCarInfo() {
        return carRepository.findAllByAccount(getUserBasicInfo());
    }

    @Transactional
    public void deleteCar(Long id) {
        Account account = getUserBasicInfo();
        Car car = carRepository.findById(id).orElseThrow(CCarNotFoundException::new);

        account.removeCar(car);
        carRepository.delete(car);
    }
}
