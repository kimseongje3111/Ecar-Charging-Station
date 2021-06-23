package com.ecar.servicestation.modules.user.service;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.dto.response.UserCarDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCarService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public void saveCar(RegisterCarRequestDto request) {
        Account account = getUserBasicInfo();
        Car car = carRepository.save(modelMapper.map(request, Car.class));

        account.addCar(car);
    }

    public List<UserCarDto> getMyCarInfo() {
        return getUserBasicInfo().getMyCars()
                .stream()
                .map(myCar -> {
                    UserCarDto userCar = modelMapper.map(myCar, UserCarDto.class);
                    userCar.setCarId(myCar.getId());

                    return userCar;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCar(long id) {
        Account account = getUserBasicInfo();
        Car car = carRepository.findCarByIdAndAccount(id, account);

        if (car == null) {
            throw new CCarNotFoundException();
        }

        carRepository.delete(account.removeCar(car));
    }

    private Account getUserBasicInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findAccountByEmail(authentication.getName()).orElseThrow(CUserNotFoundException::new);
    }
}
