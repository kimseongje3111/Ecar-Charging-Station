package com.ecar.servicestation.modules.ecar.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"(8) E_CAR CHARGING SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar/charge")
public class ECarChargingApiController {

    //TODO: 충전 시작 요청
    //TODO: 충전 종료 요청
}
