package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.ecar.dto.response.books.ReservationStatementDto;
import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.dto.response.users.UserBookmarkDto;
import com.ecar.servicestation.modules.user.dto.response.users.UserHistoryDto;
import com.ecar.servicestation.modules.user.service.UserMainService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Api(tags = {"(5) USER MAIN SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserMainApiController {

    private final UserMainService userMainService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "최근 검색 목록(충전소) 조회", notes = "최근 검색 목록(충전소) 조회")
    @GetMapping("/histories")
    public ListResult<UserHistoryDto> getUserHistories(
            @ApiParam(value = "페이지") @PageableDefault(sort = "searchedAt", direction = DESC) Pageable pageable) {

        return responseService.getListResult(userMainService.getUserHistories(pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 등록", notes = "즐겨찾기(충전소) 등록 요청")
    @PutMapping("/bookmark/{id}")
    public CommonResult registerUserBookmark(@ApiParam(value = "충전소 ID") @PathVariable Long id) {
        userMainService.registerUserBookmark(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 삭제", notes = "즐겨찾기(충전소) 삭제 요청")
    @DeleteMapping("/bookmark/{id}")
    public CommonResult deleteUserBookmark(@ApiParam(value = "충전소 ID") @PathVariable Long id) {
        userMainService.deleteUserBookmark(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "즐겨찾기(충전소) 목록 조회", notes = "즐겨찾기(충전소) 목록 조회 요청")
    @GetMapping("/bookmarks")
    public ListResult<UserBookmarkDto> getUserBookmarks(
            @ApiParam(value = "페이지") @PageableDefault(sort = "registeredAt", direction = DESC) Pageable pageable) {

        return responseService.getListResult(userMainService.getUserBookmarks(pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 예약/충전 목록 조회", notes = "사용자 예약/충전 목록 조회 요청")
    @GetMapping("/reservation-statements")
    public ListResult<ReservationStatementDto> getUserReservationStatements(
            @ApiParam(value = "상태(예약됨[0], 충전중[1], 완료됨[2])", required = true) @RequestParam @Pattern(regexp = "^[012]$") String state) {

        return responseService.getListResult(userMainService.getUserReservationStatements(state));
    }

}
