package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.dto.request.ConfirmBankRequest;
import com.ecar.servicestation.modules.user.dto.request.RegisterBankRequest;
import com.ecar.servicestation.modules.user.dto.response.RegisterBankResponse;
import com.ecar.servicestation.modules.user.service.UserBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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

    // 계좌 조회
    // 계좌 삭제
    // 주계좌 설정
    // 금액 충전
    // 금액 반환

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 등록", notes = "사용자 계좌 등록 및 인증 메시지 요청")
    @PostMapping("/register")
    public SingleResult<RegisterBankResponse> registerMyBank(@RequestBody @Valid RegisterBankRequest registerBankRequest) {
        return responseService.getSingleResult(userBankService.saveBank(registerBankRequest));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 계좌 인증", notes = "사용자 계좌 인증 요청")
    @PostMapping("/confirm")
    public CommonResult confirmMyBank(@RequestBody @Valid ConfirmBankRequest confirmBankRequest) {
        userBankService.confirmBank(confirmBankRequest);

        return responseService.getSuccessResult();
    }
}
