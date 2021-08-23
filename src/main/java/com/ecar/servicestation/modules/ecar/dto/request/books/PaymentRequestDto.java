package com.ecar.servicestation.modules.ecar.dto.request.books;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PaymentRequestDto {

    @ApiParam(value = "예약 ID", required = true)
    @NotNull
    private Long reservationId;

    @ApiParam(value = "결제 비밀번호", required = true)
    @NotBlank
    private String paymentPassword;

    @ApiParam(value = "사용 포인트")
    private Integer usedCashPoint;

}
