package com.ecar.servciestation.modules.main.dto;

import com.ecar.servciestation.modules.main.service.ResponseService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult {

    private boolean success;

    private int responseCode;

    private String message;

    public void setResponse(ApiCommonResponse response) {
        this.responseCode = response.responseCode;
        this.message = response.message;
    }
}
