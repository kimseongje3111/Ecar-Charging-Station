package com.ecar.servicestation.infra.notification.service;

public interface PushMessageService {

    void sendPushMessageTo(String targetToken, String title, String body);

}
