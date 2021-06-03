package com.ecar.servicestation.infra.data.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime unmarshal(String dateTime) throws Exception {
        return LocalDateTime.parse(dateTime, formatter);
    }

    @Override
    public String marshal(LocalDateTime dateTime) throws Exception {
        return dateTime.format(formatter);
    }
}
