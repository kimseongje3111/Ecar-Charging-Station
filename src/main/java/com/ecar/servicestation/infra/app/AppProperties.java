package com.ecar.servicestation.infra.app;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("app")
public class AppProperties {

    private String host;

    private int fastChargingFares;

    private int slowChargingFares;
}
