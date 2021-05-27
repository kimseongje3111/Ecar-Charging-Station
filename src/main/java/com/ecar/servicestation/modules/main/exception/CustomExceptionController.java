package com.ecar.servicestation.modules.main.exception;

import com.ecar.servicestation.infra.auth.exception.CAuthenticationEntryPointException;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exception")
public class CustomExceptionController {

    @GetMapping("/entry-point")
    public CommonResult entryPointException() {
        throw new CAuthenticationEntryPointException();
    }

    @GetMapping("/access-denied")
    public CommonResult accessDeniedException() {
        throw new AccessDeniedException("");
    }
}
