package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.dto.request.banks.CashInRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.CashOutRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.AuthBankRequestDto;
import com.ecar.servicestation.modules.user.dto.request.banks.RegisterBankRequestDto;
import com.ecar.servicestation.modules.user.dto.response.RegisterBankResponseDto;
import com.ecar.servicestation.modules.user.dto.response.users.UserBankDto;
import com.ecar.servicestation.modules.user.service.UserBankService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(4) USER BANK SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/bank")
public class UserBankApiController {

    private final UserBankService userBankService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 등록", notes = "사용자 계좌 등록 및 인증 메시지 요청")
    @PostMapping("/register")
    public SingleResult<RegisterBankResponseDto> registerUserBankAccount(@RequestBody @Valid RegisterBankRequestDto request) {
        return responseService.getSingleResult(userBankService.registerUserBankAccount(request));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 삭제", notes = "사용자 계좌 삭제 요청")
    @DeleteMapping("/{id}")
    public CommonResult deleteUserBankAccount(@ApiParam(value = "계좌 ID") @PathVariable Long id) {
        userBankService.deleteUserBankAccount(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 목록 조회", notes = "사용자 계좌 목록 조회 요청")
    @GetMapping("")
    public ListResult<UserBankDto> getUserBankAccounts() {
        return responseService.getListResult(userBankService.getUserBankAccounts());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 인증", notes = "사용자 계좌 인증 요청")
    @PostMapping("/auth")
    public CommonResult authenticationUserBankAccount(@RequestBody @Valid AuthBankRequestDto request) {
        userBankService.validateAuthAndConfirmUserBankAccount(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "주사용 계좌 변경", notes = "주사용 계좌 변경 요청")
    @PostMapping("/main-used/{id}")
    public CommonResult changeMainUsedBankAccount(@ApiParam(value = "계좌 ID") @PathVariable Long id) {
        userBankService.changeMainUsedBank(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "현금(캐쉬) 충전", notes = "주사용 계좌로부터 현금(캐쉬) 충전 요청")
    @PostMapping("/cash-in")
    public CommonResult chargeCashFromMainUsedBankAccount(@RequestBody @Valid CashInRequestDto request) {
        userBankService.chargeCashFromMainUsedBankAccount(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "현금(캐쉬) 환불", notes = "주사용 계좌로 현금(캐쉬) 환불 요청")
    @PostMapping("/cash-out")
    public CommonResult refundCashToMainUsedBankAccount(@RequestBody @Valid CashOutRequestDto request) {
        userBankService.refundCashToMainUsedBankAccount(request);

        return responseService.getSuccessResult();
    }

}
