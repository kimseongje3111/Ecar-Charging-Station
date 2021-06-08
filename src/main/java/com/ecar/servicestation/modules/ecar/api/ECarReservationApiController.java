package com.ecar.servicestation.modules.ecar.api;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"(6) E_CAR RESERVATION SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar")
public class ECarReservationApiController {

    // TODO(1) : 충전기 예약
    // TODO(2) : 충전기 예약 취소
    // TODO(3) : 충전기 예약 목록 조회


}
