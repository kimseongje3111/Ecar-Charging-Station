package com.ecar.servciestation.modules.main.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SingleResult<T> extends CommonResult {

    private T data;
}
