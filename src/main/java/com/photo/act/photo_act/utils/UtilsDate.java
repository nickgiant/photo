package com.photo.act.photo_act.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UtilsDate {

    public String calcDateTimeFromLong(Long datetime, String timeZoneId) {

        Instant instant = Instant.ofEpochMilli(datetime);
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(instant, ZoneId.of(timeZoneId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return localDateTime.format(formatter);
    }
}
