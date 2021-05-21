package com.ecar.servciestation.modules.main.dto;

public enum ApiCommonResponse {
    SUCCESS(0, "성공하였습니다.");

    int responseCode;
    String message;

    ApiCommonResponse(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }
}
