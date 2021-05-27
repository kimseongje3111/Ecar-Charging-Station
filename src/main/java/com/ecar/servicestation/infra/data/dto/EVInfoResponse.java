package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "response")
public class EVInfoResponse {

    @XmlElement(name = "header")
    private EVInfoHeader evInfoHeader;

    @XmlElement(name = "body")
    private EVInfoBody evInfoBody;

}
