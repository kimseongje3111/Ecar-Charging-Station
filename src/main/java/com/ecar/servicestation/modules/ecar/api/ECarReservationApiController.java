package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.dto.request.PaymentRequestDto;
import com.ecar.servicestation.modules.ecar.dto.request.ReserveRequestDto;
import com.ecar.servicestation.modules.ecar.dto.response.ChargerTimeTableDto;
import com.ecar.servicestation.modules.ecar.dto.response.ReservationStatementDto;
import com.ecar.servicestation.modules.ecar.dto.response.ReserveResponseDto;
import com.ecar.servicestation.modules.ecar.service.ECarReservationService;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
    @ApiOperation(value = "충전기 예약 시간 테이블 조회", notes = "충전기 예약 시간 테이블(최대 1주) 조회 요청")
    @GetMapping("/{id}")
    public SingleResult<ChargerTimeTableDto> getAvailableTimeTable(
            @ApiParam(value = "충전기 ID") @PathVariable Long id,
            @ApiParam(value = "페이지 번호(MAX:7)") @RequestParam @Pattern(regexp = "^[0-7]$") Integer day) {

        return responseService.getSingleResult(eCarReservationService.getChargerTimeTable(id, day));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "충전기 예약", notes = "충전기 예약(결제 전) 요청")
    @PostMapping("")
    public SingleResult<ReserveResponseDto> reserveCharger(@RequestBody @Valid ReserveRequestDto reserveRequest) {
        return responseService.getSingleResult(eCarReservationService.reserveCharger(reserveRequest));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "충전기 예약 결제", notes = "충전기 예약 결제 요청")
    @PostMapping("/payment")
    public SingleResult<ReservationStatementDto> payFaresOfReservation(@RequestBody @Valid PaymentRequestDto paymentRequest) {
        return responseService.getSingleResult(eCarReservationService.paymentIn(paymentRequest));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "충전기 예약 취소", notes = "충전기 예약 취소 요청")
    @DeleteMapping("")
    public SingleResult<ReservationStatementDto> cancelReservation(
            @ApiParam(value = "예약명") @RequestParam @NotBlank String reserveTitle) {

        return responseService.getSingleResult(eCarReservationService.cancelReservation(reserveTitle));
    }

    // TODO: 종료 알림 여부 및 종료 알림 시간 설정
}
