package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateNotificationRequest;
import com.ecar.servicestation.modules.user.dto.request.users.UpdatePasswordRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.UpdateUserRequestDto;
import com.ecar.servicestation.modules.user.service.UserBasicService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(2) USER BASIC SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserBasicApiController {

    private final UserBasicService userBasicService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 정보 조회", notes = "사용자 개인 정보 조회 요청")
    @GetMapping("")
    public SingleResult<Account> getUserInfo() {
        return responseService.getSingleResult(userBasicService.getLoginUserContext());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 정보 변경", notes = "사용자 정보 변경 요청")
    @PostMapping("")
    public CommonResult updateUserInfo(@RequestBody @Valid UpdateUserRequestDto request) {
        userBasicService.updateUserInfo(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 비밀번호 변경", notes = "사용자 비밀번호 변경 요청")
    @PostMapping("/password")
    public CommonResult updateUserPassword(@RequestBody @Valid UpdatePasswordRequestDto request) {
        userBasicService.updateUserPassword(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 알림 설정 변경", notes = "사용자 알림 설정 변경 요청")
    @PostMapping("/notification")
    public CommonResult updateUserNotificationSetting(@RequestBody @Valid UpdateNotificationRequest request) {
        userBasicService.updateUserNotificationSetting(request);

        return responseService.getSuccessResult();
    }

}
