package com.ecar.servicestation.infra.data.dto;

import com.ecar.servicestation.infra.data.dto.adapter.CustomLocalDateTimeAdapter;
import com.ecar.servicestation.modules.ecar.domain.Charger;
import com.ecar.servicestation.modules.ecar.domain.Station;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

@Data
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode(of = "chargerNumber")
public class EVInfoDto {

    @XmlElement(name = "csId")
    private Long stationNumber;

    @XmlElement(name = "addr")
    private String stationAddress;

    @XmlElement(name = "csNm")
    private String stationName;

    @XmlElement(name = "lat")
    private Double latitude;

    @XmlElement(name = "longi")
    private Double longitude;

    @XmlElement(name = "cpId")
    private Long chargerNumber;

    @XmlElement(name = "cpNm")
    private String chargerName;

    @XmlElement(name = "chargeTp")
    private Integer chargerType;

    @XmlElement(name = "cpTp")
    private Integer chargerMode;

    @XmlElement(name = "cpStat")
    private Integer chargerState;

    @XmlElement(name = "statUpdateDatetime")
    @XmlJavaTypeAdapter(CustomLocalDateTimeAdapter.class)
    private LocalDateTime chargerStateUpdatedAt;

    public Station EVInfoToStation() {
        Station station = new Station();
        station.setStationNumber(this.stationNumber);
        station.setStationAddress(this.stationAddress);
        station.setStationName(this.stationName);
        station.setLatitude(this.latitude);
        station.setLongitude(this.longitude);

        return station;
    }

    public Charger EVInfoToCharger() {
        Charger charger = new Charger();
        charger.setChargerNumber(this.chargerNumber);
        charger.setChargerName(this.chargerName);
        charger.setType(this.chargerType);
        charger.setMode(this.chargerMode);
        charger.setState(this.chargerState);
        charger.setStateUpdatedAt(this.chargerStateUpdatedAt);

        return charger;
    }

}
