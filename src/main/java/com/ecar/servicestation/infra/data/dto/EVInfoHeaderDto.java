package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "header")
public class EVInfoHeaderDto {

    private String resultCode;

    private String resultMsg;

}
