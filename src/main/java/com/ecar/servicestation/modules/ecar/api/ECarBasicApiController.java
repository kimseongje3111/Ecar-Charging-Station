package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.StationInfo;
import com.ecar.servicestation.modules.ecar.service.ECarBasicService;
import com.ecar.servicestation.modules.main.dto.SingleResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"(4) E_CAR BASIC SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar")
public class ECarBasicApiController {

    private final ECarBasicService eCarBasicService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 단건 조회", notes = "전기차 충전소 단건 조회 요청")
    @GetMapping("/station/{id}")
    public SingleResult<StationInfo> getStationInfo(@PathVariable @ApiParam(value = "충전소 ID") Long id) {
        return responseService.getSingleResult(eCarBasicService.getStationInfo(id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 단건 조회 및 기록 저장", notes = "전기차 충전소 단건 조회 및 기록 저장 요청")
    @GetMapping("/station/{id}/register")
    public SingleResult<StationInfo> getChargerInfoAndSaveHistory(@PathVariable @ApiParam(value = "충전기 ID") Long id) {
        return responseService.getSingleResult(eCarBasicService.getChargerInfoAndSaveHistory(id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소의 충전기 단건 조회", notes = "전기차 충전소의 충전기 단건 조회 요청")
    @GetMapping("/charger/{id}")
    public SingleResult<Charger> getChargerInfo(@PathVariable @ApiParam(value = "충전기 ID") Long id) {
        return responseService.getSingleResult(eCarBasicService.getChargerInfo(id));
    }

}
