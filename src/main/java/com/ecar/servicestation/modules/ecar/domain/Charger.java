package com.ecar.servicestation.modules.ecar.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Charger {

    @Id
    @GeneratedValue
    @Column(name = "charger_id")
    private Long id;

    // 충전기 정보 //

    @Column(nullable = false, unique = true)
    private Long chargerNumber;

    private String chargerName;

    private Integer type;

    private Integer mode;

    private Integer state;

    private LocalDateTime stateUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    public boolean isRequiredUpdate(LocalDateTime localDateTime) {
        return this.stateUpdatedAt.isBefore(localDateTime);
    }

}
