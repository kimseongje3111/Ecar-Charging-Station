package com.ecar.servicestation.modules.main.exception;

import com.ecar.servicestation.infra.auth.exception.CAuthenticationEntryPointException;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.data.exception.EVInfoServiceException;
import com.ecar.servicestation.infra.map.exception.ReverseGeoCodingException;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.exception.CUserLoginFailedException;
import com.ecar.servicestation.modules.user.exception.CUserNotFoundException;
import com.ecar.servicestation.modules.user.exception.CUserSignUpFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import java.nio.file.AccessDeniedException;

import static java.lang.Integer.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice {

    private final ResponseService responseService;
    private final MessageSource messageSource;

    // 유저 서비스 예외 //

    @ExceptionHandler(CUserNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult userNotFound(HttpServletRequest request, CUserNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("userNotFound.code")), getMessage("userNotFound.msg"));
    }

    @ExceptionHandler(CUserLoginFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult loginFailed(HttpServletRequest request, CUserLoginFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("loginFailed.code")), getMessage("loginFailed.msg"));
    }

    @ExceptionHandler(CUserSignUpFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult signUpFailed(HttpServletRequest request, CUserSignUpFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("signUpFailed.code")), getMessage("signUpFailed.msg"));
    }

    // 외부 API 예외 //

    @ExceptionHandler(EVINfoNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult evInfoNotFoundException(HttpServletRequest request, EVINfoNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("evInfoNotFoundException.code")), getMessage("evInfoNotFoundException.msg"));
    }

    @ExceptionHandler(EVInfoServiceException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult evInfoServiceException(HttpServletRequest request, EVInfoServiceException e) {
        return responseService.getFailedResult(parseInt(getMessage("evInfoServiceException.code")), getMessage("evInfoServiceException.msg"));
    }

    @ExceptionHandler(ReverseGeoCodingException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult reverseGeoCodingException(HttpServletRequest request, ReverseGeoCodingException e) {
        return responseService.getFailedResult(parseInt(getMessage("reverseGeoCodingException.code")), getMessage("reverseGeoCodingException.msg"));
    }

    // 인증 예외 //

    @ExceptionHandler(CAuthenticationEntryPointException.class)
    @ResponseStatus(FORBIDDEN)
    public CommonResult authenticationEntryPointException(HttpServletRequest request, CAuthenticationEntryPointException e) {
        return responseService.getFailedResult(parseInt(getMessage("entryPointException.code")), getMessage("entryPointException.msg"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(FORBIDDEN)
    public CommonResult accessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        return responseService.getFailedResult(parseInt(getMessage("accessDeniedException.code")), getMessage("accessDeniedException.msg"));
    }

    private String getMessage(String value) {
        return getMessage(value, null);
    }

    private String getMessage(String value, Object[] args) {
        return messageSource.getMessage(value, args, LocaleContextHolder.getLocale());
    }
}
