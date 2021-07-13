package com.ecar.servicestation.infra.address.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"siNm", "sggNm", "rn"})
public class AddressDto {

    private String siNm;

    private String sggNm;

    private String emdNm;

    private String rn;

    public String getOldAddress() {
        return siNm + " " + sggNm + " " + emdNm;
    }

    public String getNewAddress() {
        return siNm + " " + sggNm + " " + rn;
    }

}
