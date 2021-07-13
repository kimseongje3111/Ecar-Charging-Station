package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.dto.request.ChargerRequestDto;
import com.ecar.servicestation.modules.ecar.service.ECarChargingService;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(9) E_CAR CHARGING SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar/charge")
public class ECarChargingApiController {

    private final ECarChargingService eCarChargingService;
    private final ResponseService responseService;

    @ApiOperation(value = "예약 충전 시작", notes = "예약 충전 시작 요청")
    @PostMapping("")
    public CommonResult startChargingReservation(@RequestBody @Valid ChargerRequestDto request) {
        eCarChargingService.checkReservationAndStartCharging(request);

        return responseService.getSuccessResult();
    }

    @ApiOperation(value = "예약 충전 종료", notes = "예약 충전 종료 요청")
    @PostMapping("/finish")
    public CommonResult finishChargingReservation(@RequestBody @Valid ChargerRequestDto request) {
        eCarChargingService.checkReservationAndEndCharging(request);

        return responseService.getSuccessResult();
    }

}
