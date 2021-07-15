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
public class DeviceToken {

    @Id
    @GeneratedValue
    @Column(name = "device_token_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceUniqueToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

}
