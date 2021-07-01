package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.ecar.dto.response.ReservationStatementDto;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.response.UserBookmarkDto;
import com.ecar.servicestation.modules.user.dto.response.UserHistory;
import com.ecar.servicestation.modules.user.service.UserBasicService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;

import static org.springframework.data.domain.Sort.Direction.DESC;

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
    @ApiOperation(value = "회원 기본 정보 조회", notes = "로그인한 회원의 기본 정보 조회 요청")
    @GetMapping("")
    public SingleResult<Account> getUserBasicInfo() {
        return responseService.getSingleResult(userBasicService.getUserBasicInfo());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "최근 검색 목록(충전소) 조회", notes = "최근 검색 목록(충전소) 조회")
    @GetMapping("/history")
    public ListResult<UserHistory> getUserHistories(
            @ApiParam(value = "페이지") @PageableDefault(value = 10, sort = "searchedAt", direction = DESC) Pageable pageable) {

        return responseService.getListResult(userBasicService.getUserHistories(pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 등록", notes = "즐겨찾기(충전소) 등록 요청")
    @PutMapping("/bookmark/{id}")
    public CommonResult registerBookmark(@ApiParam(value = "충전소 ID") @PathVariable Long id) {
        userBasicService.saveBookmark(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 목록 조회", notes = "즐겨찾기(충전소) 목록 조회 요청")
    @GetMapping("/bookmark")
    public ListResult<UserBookmarkDto> getUserBookmark(
            @ApiParam(value = "페이지") @PageableDefault(value = 10, sort = "registeredAt", direction = DESC) Pageable pageable) {

        return responseService.getListResult(userBasicService.getUserBookmark(pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 삭제", notes = "즐겨찾기(충전소) 삭제 요청")
    @DeleteMapping("/bookmark/{id}")
    public CommonResult removeBookmark(@ApiParam(value = "충전소 ID") @PathVariable Long id) {
        userBasicService.deleteBookmark(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 예약/충전 목록 조회", notes = "사용자 예약/충전 목록 조회 요청")
    @GetMapping("/reservation-statements")
    public ListResult<ReservationStatementDto> getUserReservationStatements(
            @ApiParam(value = "상태(예약됨[0], 충전중[1], 완료됨[2])", required = true) @RequestParam @Pattern(regexp = "^[012]$") String state) {

        return responseService.getListResult(userBasicService.getUserReservationStatements(state));
    }
}
