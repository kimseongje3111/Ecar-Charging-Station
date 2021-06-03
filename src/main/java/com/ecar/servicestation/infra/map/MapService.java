package com.ecar.servicestation.infra.map;

import com.ecar.servicestation.infra.map.dto.MapLocation;

public interface MapService {

    MapLocation geoCoding(String address);

    String reverseGeoCoding(MapLocation location);
}
