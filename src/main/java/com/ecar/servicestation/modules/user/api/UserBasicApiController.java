package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Account;
import com.ecar.servicestation.modules.user.service.UserBasicService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // TODO : 최근 검색 목록 조회
    // TODO : 충전소 즐겨 찾기 등록/조회/삭제
}
