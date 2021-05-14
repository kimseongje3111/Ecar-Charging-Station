package com.ecar.servciestation.modules.Ecar.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Charger {

    @Id
    @GeneratedValue
    @Column(name = "charger_id")
    private Long id;

    // 충전기 정보 //

    private Long chargerNumber;

    private String chargerName;

    private Integer mode;

    private Integer state;

    private LocalDateTime stateUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;
}
