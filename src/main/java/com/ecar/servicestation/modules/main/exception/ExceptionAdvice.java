package com.ecar.servicestation.modules.main.exception;

import com.ecar.servicestation.infra.address.exception.AddressServiceException;
import com.ecar.servicestation.infra.auth.exception.CAuthenticationEntryPointException;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.data.exception.EVInfoServiceException;
import com.ecar.servicestation.infra.map.exception.MapServiceException;
import com.ecar.servicestation.modules.ecar.exception.CChargerNotFoundException;
import com.ecar.servicestation.modules.ecar.exception.CStationNotFoundException;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // 요청 파라미터 예외 //
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    protected CommonResult methodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException e) {
        return responseService.getFailedResult(parseInt(getMessage("methodArgumentNotValid.code")), getMessage("methodArgumentNotValid.msg"));
    }

    // 서비스 예외 //

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

    @ExceptionHandler(CStationNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult stationNotFound(HttpServletRequest request, CStationNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("stationNotFound.code")), getMessage("stationNotFound.msg"));
    }

    @ExceptionHandler(CChargerNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult chargerNotFound(HttpServletRequest request, CChargerNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("chargerNotFound.code")), getMessage("chargerNotFound.msg"));
    }

    @ExceptionHandler(CBookmarkFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult bookmarkFailed(HttpServletRequest request, CBookmarkFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("bookmarkFailed.code")), getMessage("bookmarkFailed.msg"));
    }

    @ExceptionHandler(CBookmarkNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult bookmarkNotFound(HttpServletRequest request, CBookmarkNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("bookmarkNotFound.code")), getMessage("bookmarkNotFound.msg"));
    }

    @ExceptionHandler(CCarNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult carNotFound(HttpServletRequest request, CCarNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("carNotFound.code")), getMessage("carNotFound.msg"));
    }

    // 외부 API 예외 //

    @ExceptionHandler(EVINfoNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult evInfoNotFound(HttpServletRequest request, EVINfoNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("evInfoNotFound.code")), getMessage("evInfoNotFound.msg"));
    }

    @ExceptionHandler(EVInfoServiceException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult evInfoServiceFailed(HttpServletRequest request, EVInfoServiceException e) {
        return responseService.getFailedResult(parseInt(getMessage("evInfoServiceFailed.code")), getMessage("evInfoServiceFailed.msg"));
    }

    @ExceptionHandler(MapServiceException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult mapServiceFailed(HttpServletRequest request, MapServiceException e) {
        return responseService.getFailedResult(parseInt(getMessage("mapServiceFailed.code")), getMessage("mapServiceFailed.msg"));
    }

    @ExceptionHandler(AddressServiceException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult addressServiceFailed(HttpServletRequest request, AddressServiceException e) {
        return responseService.getFailedResult(parseInt(getMessage("addressServiceFailed.code")), getMessage("addressServiceFailed.msg"));
    }

    // 인증 예외 //

    @ExceptionHandler(CAuthenticationEntryPointException.class)
    @ResponseStatus(FORBIDDEN)
    public CommonResult authenticationEntryPointException(HttpServletRequest request, CAuthenticationEntryPointException e) {
        return responseService.getFailedResult(parseInt(getMessage("entryPoint.code")), getMessage("entryPoint.msg"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(FORBIDDEN)
    public CommonResult accessDenied(HttpServletRequest request, AccessDeniedException e) {
        return responseService.getFailedResult(parseInt(getMessage("accessDenied.code")), getMessage("accessDenied.msg"));
    }

    private String getMessage(String value) {
        return getMessage(value, null);
    }

    private String getMessage(String value, Object[] args) {
        return messageSource.getMessage(value, args, LocaleContextHolder.getLocale());
    }
}
