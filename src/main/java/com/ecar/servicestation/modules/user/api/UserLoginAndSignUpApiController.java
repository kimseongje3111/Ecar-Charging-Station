package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.request.LoginRequest;
import com.ecar.servicestation.modules.user.dto.request.SignUpRequest;
import com.ecar.servicestation.modules.user.service.UserLoginAndSignUpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Api(tags = {"(1) USER SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserLoginAndSignUpApiController {

    private final UserLoginAndSignUpService userLoginAndSignUpService;
    private final ResponseService responseService;

    @ApiOperation(value = "로그인", notes = "로그인 요청, 로그인 성공 시 인증 토큰(JWT) 발급")
    @PostMapping("/login")
    public SingleResult<String> login(@RequestBody @Valid LoginRequest request) {
        return responseService.getSingleResult(userLoginAndSignUpService.login(request));
    }

    @ApiOperation(value = "회원가입", notes = "회원가입 요청")
    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody @Valid SignUpRequest request) {
        Account account = userLoginAndSignUpService.signUp(request);
        userLoginAndSignUpService.sendEmailForAuthentication(account);

        return responseService.getSuccessResult();
    }

    @ApiOperation(value = "이메일 계정 인증", notes = "회원가입 후 이메일 계정 인증 요청")
    @PostMapping("/email-auth-token")
    public CommonResult emailAuthentication(
            @ApiParam(value = "회원 이메일", required = true) @RequestParam @NotBlank @Email String email,
            @ApiParam(value = "이메일 계정 인증 토큰", required = true) @RequestParam @NotBlank String token) {

        userLoginAndSignUpService.validateEmailAuthToken(email, token);

        return responseService.getSuccessResult();
    }
}
