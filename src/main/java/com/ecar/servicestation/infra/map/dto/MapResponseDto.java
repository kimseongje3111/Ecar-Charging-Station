package com.ecar.servicestation.infra.map.dto;

import lombok.Data;

import java.util.List;

@Data
public class MapResponseDto {

    private MapErrorDto error;

    private MapStatusDto status;

    private List<MapResultDto> results;

}
