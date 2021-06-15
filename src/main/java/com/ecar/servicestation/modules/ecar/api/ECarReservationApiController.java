package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.dto.request.ReserveRequest;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerTimeTable;
import com.ecar.servicestation.modules.ecar.dto.response.ReserveResponse;
import com.ecar.servicestation.modules.ecar.service.ECarReservationService;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@Api(tags = {"(7) E_CAR RESERVATION SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar/reserve")
public class ECarReservationApiController {

    private final ECarReservationService eCarReservationService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "충전기 예약 시간 테이블 조회", notes = "충전기 예약 시간 테이블 조회 요청")
    @GetMapping("/{id}")
    public SingleResult<ChargerTimeTable> getAvailableTimeTable(
            @ApiParam(value = "충전기 ID") @PathVariable Long id,
            @ApiParam(value = "페이지 번호(MAX:7)", defaultValue = "0") @RequestParam @Pattern(regexp = "^[0-7]$") Integer day) {

        return responseService.getSingleResult(eCarReservationService.getChargerTimeTable(id, day));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "충전기 예약", notes = "충전기 예약 요청")
    @PostMapping("")
    public SingleResult<ReserveResponse> reserveCharger(@RequestBody @Valid ReserveRequest reserveRequest) {
        return responseService.getSingleResult(eCarReservationService.reserveCharger(reserveRequest));
    }

    // 결제
    // 예약 취소
}
