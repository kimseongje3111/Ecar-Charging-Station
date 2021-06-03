package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "response")
public class EVInfoResponse {

    private EVInfoHeader header;

    private EVInfoBody body;

}
