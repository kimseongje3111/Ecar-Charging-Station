package com.ecar.servicestation.infra.data.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "body")
public class EVInfoBody {

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<EVInfo> evInfoList;

    @XmlElement(name = "numOfRows")
    public Integer numOfRows;

    @XmlElement(name = "pageNo")
    public Integer pageNo;

    @XmlElement(name = "totalCount")
    public Integer totalCount;

}
