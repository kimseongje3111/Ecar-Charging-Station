package com.ecar.servicestation.infra.notification.service;

import com.ecar.servicestation.infra.notification.dto.FcmMessageDto;
import com.ecar.servicestation.infra.notification.dto.MessageDto;
import com.ecar.servicestation.infra.notification.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class FireBaseCloudMessageService implements PushMessageService {

    private final ObjectMapper objectMapper;

    @Value("${external_api.firebase.base_url}")
    private String BASE_URL;

    @Value("${external_api.firebase.project_id}")
    private String PROJECT_ID;

    @Override
    public void sendPushMessageTo(String targetToken, String title, String body) {
        OkHttpClient client = new OkHttpClient();
        FcmMessageDto fcmMessage = makePushMessage(targetToken, title, body);

        try {
            Request request =
                    new Request.Builder()
                            .url(makeServiceUrl())
                            .post(
                                    RequestBody.create(objectMapper.writeValueAsBytes(fcmMessage), MediaType.get("application/json; charset=utf-8"))
                            )
                            .addHeader(HttpHeaders.AUTHORIZATION, "Bear " + getAccessToken())
                            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                            .build();

            Response response = client.newCall(request).execute();

            log.info("FCM response : {}", Objects.requireNonNull(response.body()).toString());

        } catch (IOException e) {
            log.error("FCM : unable to send push message.");
        }
    }

    private String makeServiceUrl() {
        return BASE_URL + "/" + PROJECT_ID + "/messages:send";
    }

    private FcmMessageDto makePushMessage(String targetToken, String title, String body) {
        NotificationDto notification = new NotificationDto();
        notification.setTitle(title);
        notification.setBody(body);

        MessageDto message = new MessageDto();
        message.setNotification(notification);
        message.setToken(targetToken);

        FcmMessageDto fcmMessage = new FcmMessageDto();
        fcmMessage.setValidate_only(false);
        fcmMessage.setMessage(message);

        return fcmMessage;
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/firebase_service_key.json";

        GoogleCredentials googleCredentials =
                GoogleCredentials
                        .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();

        return googleCredentials.getAccessToken().getTokenValue();
    }

}

