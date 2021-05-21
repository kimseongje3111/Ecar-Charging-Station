package com.ecar.servciestation.modules.main.service;

import com.ecar.servciestation.modules.main.dto.ApiCommonResponse;
import com.ecar.servciestation.modules.main.dto.CommonResult;
import com.ecar.servciestation.modules.main.dto.SingleResult;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    public CommonResult getFailedResult(int code, String message) {
        return new CommonResult(false, code, message);
    }

    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();

        setSuccessResponse(result);

        return result;
    }

    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);

        setSuccessResponse(result);

        return result;
    }

    private void setSuccessResponse(CommonResult result) {
        result.setSuccess(true);
        result.setResponse(ApiCommonResponse.SUCCESS);
    }
}
