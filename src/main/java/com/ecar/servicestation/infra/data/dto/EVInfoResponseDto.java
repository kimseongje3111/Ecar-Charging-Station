package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "response")
public class EVInfoResponseDto {

    private EVInfoHeaderDto header;

    private EVInfoBodyDto body;

}
