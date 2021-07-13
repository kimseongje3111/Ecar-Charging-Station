package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequestDto;
import com.ecar.servicestation.modules.user.dto.response.UserCarDto;
import com.ecar.servicestation.modules.user.service.UserCarService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(3) USER CAR SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/car")
public class UserCarApiController {

    private final UserCarService userCarService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 차량 등록", notes = "사용자 차랑 등록 요청")
    @PostMapping("/register")
    public CommonResult registerUserCar(@RequestBody @Valid RegisterCarRequestDto request) {
        userCarService.registerUserCar(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 차량 삭제", notes = "사용자 차랑 삭제 요청")
    @DeleteMapping("/{id}")
    public CommonResult deleteUserCar(@ApiParam(value = "사용자 차량 ID") @PathVariable Long id) {
        userCarService.deleteUserCar(id);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 차량 조회", notes = "사용자 차랑 조회 요청")
    @GetMapping("")
    public ListResult<UserCarDto> getUserCars() {
        return responseService.getListResult(userCarService.getUserCars());
    }

}
