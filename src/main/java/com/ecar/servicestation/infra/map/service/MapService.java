package com.ecar.servicestation.infra.map.service;

import com.ecar.servicestation.infra.map.dto.MapLocationDto;

public interface MapService {

    String reverseGeoCoding(MapLocationDto location);
}
