package com.ecar.servicestation.infra.address.dto;

import lombok.Data;

@Data
public class AddressCommon {

    private String errorMessage;

    private String errorCode;

    private Integer countPerPage;

    private Integer currentPage;

    private String totalCount;
}
