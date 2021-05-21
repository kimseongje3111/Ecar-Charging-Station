package com.ecar.servciestation.modules.user.api;

import com.ecar.servciestation.modules.main.dto.CommonResult;
import com.ecar.servciestation.modules.main.dto.SingleResult;
import com.ecar.servciestation.modules.main.service.ResponseService;
import com.ecar.servciestation.modules.user.domain.Account;
import com.ecar.servciestation.modules.user.dto.LoginRequestDto;
import com.ecar.servciestation.modules.user.dto.SignUpRequestDto;
import com.ecar.servciestation.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserApiController {

    private final UserService userService;
    private final ResponseService responseService;

    @PostMapping("/login")
    public SingleResult<String> login(@RequestBody LoginRequestDto request) {
        String token = userService.login(request);

        return responseService.getSingleResult(token);
    }

    @PostMapping("/sign-up")
    public CommonResult signUp(@RequestBody SignUpRequestDto request) {
        Account account = userService.signUp(request);

        userService.sendEmailForAuthentication(account);

        return responseService.getSuccessResult();
    }

    @PostMapping("/email-auth-token")
    public CommonResult emailAuthentication(@RequestParam String email, @RequestParam String token) {
        userService.validateEmailAuthToken(email, token);

        return responseService.getSuccessResult();
    }
}
