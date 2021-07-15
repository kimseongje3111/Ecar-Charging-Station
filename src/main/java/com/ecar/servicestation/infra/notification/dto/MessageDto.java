package com.ecar.servicestation.infra.notification.dto;

import lombok.Data;

@Data
public class MessageDto {

    private NotificationDto notification;

    private String token;

}
