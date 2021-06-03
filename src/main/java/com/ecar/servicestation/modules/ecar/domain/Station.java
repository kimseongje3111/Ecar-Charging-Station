package com.ecar.servicestation.modules.ecar.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Station {

    @Id
    @GeneratedValue
    @Column(name = "station_id")
    private Long id;

    // 충전소 정보 //

    @Column(nullable = false, unique = true)
    private Long stationNumber;

    private String stationName;

    private String stationAddress;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL)
    @JsonProperty(access = WRITE_ONLY)
    private Set<Charger> chargers = new HashSet<>();

    // 연관 관계 설정 메서드 //

    public void addCharger(Charger charger) {
        this.chargers.add(charger);
        charger.setStation(this);
    }
}
