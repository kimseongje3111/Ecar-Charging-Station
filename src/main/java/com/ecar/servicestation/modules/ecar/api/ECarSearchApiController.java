package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchLocationDto;
import com.ecar.servicestation.modules.ecar.dto.request.searchs.SearchConditionDto;
import com.ecar.servicestation.modules.ecar.service.ECarSearchService;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"(7) E_CAR SEARCH SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar/find")
public class ECarSearchApiController {

    private final ECarSearchService eCarSearchService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 검색", notes = "입력 주소 및 검색 조건에 맞는 주변 전기차 충전소 정보 요청")
    @GetMapping("")
    public ListResult<Charger> searchECarChargingStation(
            @Valid SearchConditionDto condition,
            @ApiParam(value = "페이지") Pageable pageable) {

        return responseService.getListResult(eCarSearchService.getSearchResultsBy(condition, pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 검색", notes = "해당 위도/경도의 주변 전기차 충전소 정보 요청")
    @GetMapping("/location")
    public ListResult<Charger> searchECarChargingStationByLocation(
            @Valid SearchLocationDto location,
            @ApiParam(value = "페이지") Pageable pageable) {

        return responseService.getListResult(eCarSearchService.getSearchResultsBy(location, pageable));
    }

}
