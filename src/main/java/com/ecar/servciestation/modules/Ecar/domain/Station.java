package com.ecar.servciestation.modules.Ecar.domain;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Station {

    @Id
    @GeneratedValue
    @Column(name = "station_id")
    private Long id;

    // 충전소 정보 //

    private Long stationNumber;

    private String stationName;

    private String stationAddress;

    private Long latitude;

    private Long longitude;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    private Set<Charger> chargers = new HashSet<>();

    // 연관 관계 설정 메서드 //

    public void addCharger(Charger charger) {
        this.chargers.add(charger);
        charger.setStation(this);
    }
}
