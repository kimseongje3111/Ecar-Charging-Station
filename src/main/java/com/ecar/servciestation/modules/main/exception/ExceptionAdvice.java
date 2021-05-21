package com.ecar.servciestation.modules.main.exception;

import com.ecar.servciestation.modules.main.dto.CommonResult;
import com.ecar.servciestation.modules.main.service.ResponseService;
import com.ecar.servciestation.modules.user.exception.CUserLoginFailedException;
import com.ecar.servciestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servciestation.modules.user.exception.CUserSignUpFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice {

    private final ResponseService responseService;
    private final MessageSource messageSource;

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult userNotFound(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.getFailedResult(Integer.parseInt(getMessage("userNotFound.code")), getMessage("userNotFound.msg"));
    }

    @ExceptionHandler(CUserLoginFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult loginFailed(HttpServletRequest request, CUserLoginFailedException e) {
        return responseService.getFailedResult(Integer.parseInt(getMessage("loginFailed.code")), getMessage("loginFailed.msg"));
    }

    @ExceptionHandler(CUserSignUpFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected CommonResult signUpFailed(HttpServletRequest request, CUserSignUpFailedException e) {
        return responseService.getFailedResult(Integer.parseInt(getMessage("signUpFailed.code")), getMessage("signUpFailed.msg"));
    }

    private String getMessage(String value) {
        return getMessage(value, null);
    }

    private String getMessage(String value, Object[] args) {
        return messageSource.getMessage(value, args, LocaleContextHolder.getLocale());
    }
}
