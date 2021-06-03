package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "header")
public class EVInfoHeader {

    private String resultCode;

    private String resultMsg;

}
