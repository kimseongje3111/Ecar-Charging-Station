package com.ecar.servicestation.infra.data.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "body")
public class EVInfoBodyDto {

    @Getter(AccessLevel.NONE)
    private List<EVInfoDto> items;

    private Integer numOfRows;

    private Integer pageNo;

    private Integer totalCount;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<EVInfoDto> getItems() {
        return items;
    }

}
