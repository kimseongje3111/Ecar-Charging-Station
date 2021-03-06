package com.ecar.servicestation.modules.user.repository;

import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsCarByCarNumber(String carNumber);

    Car findCarByIdAndAccount(long carId, Account account);

    List<Car> findAllByAccount(Account account);

}
