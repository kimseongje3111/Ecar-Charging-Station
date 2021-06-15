package com.ecar.servicestation.modules.user.api;

import com.ecar.servicestation.modules.main.dto.CommonResult;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import com.ecar.servicestation.modules.user.domain.Car;
import com.ecar.servicestation.modules.user.dto.request.RegisterCarRequest;
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
    public CommonResult registerMyCar(@RequestBody @Valid RegisterCarRequest request) {
        userCarService.saveCar(request);

        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 차량 조회", notes = "사용자 차랑 조회 요청")
    @GetMapping("")
    public ListResult<Car> getMyCarInfo() {
        return responseService.getListResult(userCarService.getMyCarInfo());
    }

    // 차량 삭제 DELETE
    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "사용자 차량 삭제", notes = "사용자 차랑 삭제 요청")
    @DeleteMapping("/{id}")
    public CommonResult deleteMyCar(@ApiParam(value = "사용자 차량 ID") @PathVariable Long id) {
        userCarService.deleteCar(id);

        return responseService.getSuccessResult();
    }
}
