package com.ecar.servicestation.infra.notification.dto;

import lombok.Data;

@Data
public class FcmMessageDto {

    private boolean validate_only;

    private MessageDto message;

}
