package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.dto.UserHistory;
import com.ecar.servicestation.modules.user.service.UserBasicService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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

}
