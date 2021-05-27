package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "header")
public class EVInfoHeader {

    @XmlElement(name = "resultCode")
    private String resultCode;

    @XmlElement(name = "resultMsg")
    private String resultMsg;

}
