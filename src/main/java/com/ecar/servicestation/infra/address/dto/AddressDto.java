package com.ecar.servicestation.infra.address.dto;

import lombok.Data;

@Data
public class AddressDto {

    private String siNm;

    private String sggNm;

    private String emdNm;

    private String rn;

    public String getOldAddress() {
        return siNm + " " + sggNm + " " + emdNm;
    }

    public String getPrefixOfNewAddress() {
        return siNm + " " + sggNm + " " + rn.replaceAll("[0-9]+[가-힣]*","").charAt(0);
    }
}
