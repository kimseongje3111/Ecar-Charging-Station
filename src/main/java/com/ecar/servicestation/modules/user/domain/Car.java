package com.ecar.servicestation.modules.user.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Car {

    @Id
    @GeneratedValue
    @Column(name = "car_id")
    private Long id;

    private String carModel;

    private String carModelYear;

    private String carType;

    @Column(nullable = false, unique = true)
    private String carNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
