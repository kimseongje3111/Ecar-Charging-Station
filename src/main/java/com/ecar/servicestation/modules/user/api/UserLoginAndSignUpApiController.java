package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.users.EmailAuthRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.LoginRequestDto;
import com.ecar.servicestation.modules.user.dto.request.users.SignUpRequestDto;
import com.ecar.servicestation.modules.user.service.UserLoginAndSignUpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(1) USER LOGIN & SIGN-UP SERVICE"})
@RestController
@RequiredArgsConstructor
public class UserLoginAndSignUpApiController {

    private final UserLoginAndSignUpService userLoginAndSignUpService;
    private final ResponseService responseService;

    @ApiOperation(value = "회원가입", notes = "회원가입 요청")
    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody @Valid SignUpRequestDto request) {
        Account newAccount = userLoginAndSignUpService.signUp(request);

        userLoginAndSignUpService.sendEmailForAuthentication(newAccount);

        return responseService.getSuccessResult();
    }

    @ApiOperation(value = "이메일 계정 인증", notes = "회원가입 후 이메일 계정 인증 요청")
    @PostMapping("/email-auth-token")
    public CommonResult emailAuthentication(@Valid EmailAuthRequestDto request) {
        userLoginAndSignUpService.validateEmailAuthToken(request);

        return responseService.getSuccessResult();
    }

    @ApiOperation(value = "로그인", notes = "로그인 요청, 로그인 성공 시 인증 토큰(JWT) 발급")
    @PostMapping("/login")
    public SingleResult<String> login(@RequestBody @Valid LoginRequestDto request) {
        return responseService.getSingleResult(userLoginAndSignUpService.login(request));
    }

}
