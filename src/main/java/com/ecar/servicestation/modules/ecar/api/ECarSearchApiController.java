package com.ecar.servicestation.modules.ecar.api;

import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.dto.SearchLocation;
import com.ecar.servicestation.modules.ecar.dto.SearchCondition;
import com.ecar.servicestation.modules.ecar.service.ECarSearchService;
import com.ecar.servicestation.modules.main.dto.ListResult;
import com.ecar.servicestation.modules.main.service.ResponseService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@Api(tags = {"(3) E_CAR SERVICE"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/ecar")
public class ECarSearchApiController {

    private final ECarSearchService eCarSearchService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 검색", notes = "입력 주소 및 검색 조건에 맞는 주변 전기차 충전소 정보 요청")
    @GetMapping("/find")
    public ListResult<Charger> searchECarChargingStation(
            SearchCondition condition,
            @ApiParam(value = "페이지") @PageableDefault(size = 10) Pageable pageable) {

        return responseService.getListResult(eCarSearchService.getSearchResults(condition, pageable));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 발급된 ACCESS_TOKEN",
                    required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "전기차 충전소 검색", notes = "해당 위도/경도의 주변 전기차 충전소 정보 요청")
    @GetMapping("/find/location")
    public ListResult<Charger> searchECarChargingStationByLocation(
            SearchLocation location,
            @ApiParam(value = "페이지") @PageableDefault(size = 10) Pageable pageable) {

        return responseService.getListResult(eCarSearchService.getSearchResultsByLocation(location, pageable));
    }
}
