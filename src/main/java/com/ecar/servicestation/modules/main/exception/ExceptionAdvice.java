package com.ecar.servicestation.modules.main.exception;

import com.ecar.servicestation.infra.address.exception.AddressExceededException;
import com.ecar.servicestation.infra.address.exception.AddressServiceException;
import com.ecar.servicestation.infra.auth.exception.CAuthenticationEntryPointException;
import com.ecar.servicestation.infra.data.exception.EVINfoNotFoundException;
import com.ecar.servicestation.infra.data.exception.EVInfoServiceException;
import com.ecar.servicestation.infra.map.exception.MapNotFoundException;
import com.ecar.servicestation.infra.map.exception.MapServiceException;
import com.ecar.servicestation.modules.ecar.exception.*;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationCancelFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationEndFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReservationStartFailedException;
import com.ecar.servicestation.modules.ecar.exception.books.CReserveFailedException;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.exception.banks.CBankAuthFailedException;
import com.ecar.servicestation.modules.user.exception.banks.CBankExistsException;
import com.ecar.servicestation.modules.user.exception.banks.CBankNotFoundException;
import com.ecar.servicestation.modules.user.exception.banks.CUserCashFailedException;
import com.ecar.servicestation.modules.user.exception.cars.CCarNotFoundException;
import com.ecar.servicestation.modules.user.exception.cars.CCarRegistrationFailedException;
import com.ecar.servicestation.modules.user.exception.users.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

    // ?????? ???????????? ?????? //

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    protected CommonResult methodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException e) {
        return responseService.getFailedResult(parseInt(getMessage("methodArgumentNotValid.code")), getMessage("methodArgumentNotValid.msg"));
    }

    // ????????? ?????? //

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

    @ExceptionHandler(CCarRegistrationFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult carRegistrationFailed(HttpServletRequest request, CCarRegistrationFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("carRegistrationFailed.code")), getMessage("carRegistrationFailed.msg"));
    }

    @ExceptionHandler(CReserveFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult reserveFailed(HttpServletRequest request, CReserveFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("reserveFailed.code")), getMessage("reserveFailed.msg"));
    }

    @ExceptionHandler(CBankNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult bankNotFound(HttpServletRequest request, CBankNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("bankNotFound.code")), getMessage("bankNotFound.msg"));
    }

    @ExceptionHandler(CBankExistsException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult bankExists(HttpServletRequest request, CBankExistsException e) {
        return responseService.getFailedResult(parseInt(getMessage("bankExists.code")), getMessage("bankExists.msg"));
    }

    @ExceptionHandler(CBankAuthFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult bankAuthFailed(HttpServletRequest request, CBankAuthFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("bankAuthFailed.code")), getMessage("bankAuthFailed.msg"));
    }

    @ExceptionHandler(CUserCashFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult userCashFailed(HttpServletRequest request, CUserCashFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("userCashFailed.code")), getMessage("userCashFailed.msg"));
    }

    @ExceptionHandler(CReservationCancelFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult reservationCancelFailed(HttpServletRequest request, CReservationCancelFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("reservationCancelFailed.code")), getMessage("reservationCancelFailed.msg"));
    }

    @ExceptionHandler(CReservationStartFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult reservationStartFailed(HttpServletRequest request, CReservationStartFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("reservationStartFailed.code")), getMessage("reservationStartFailed.msg"));
    }

    @ExceptionHandler(CReservationEndFailedException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    protected CommonResult reservationEndFailed(HttpServletRequest request, CReservationEndFailedException e) {
        return responseService.getFailedResult(parseInt(getMessage("reservationEndFailed.code")), getMessage("reservationEndFailed.msg"));
    }

    // ?????? API ?????? //

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

    @ExceptionHandler(MapNotFoundException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult mapNotFound(HttpServletRequest request, MapNotFoundException e) {
        return responseService.getFailedResult(parseInt(getMessage("mapNotFound.code")), getMessage("mapNotFound.msg"));
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

    @ExceptionHandler(AddressExceededException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public CommonResult addressExceeded(HttpServletRequest request, AddressExceededException e) {
        return responseService.getFailedResult(parseInt(getMessage("addressExceeded.code")), getMessage("addressExceeded.msg"));
    }


    // ?????? ?????? //

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
