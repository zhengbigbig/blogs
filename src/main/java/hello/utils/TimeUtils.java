package hello.utils;

import hello.configuration.ConstantConfig;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TimeUtils {

    public static String formatInstantToDateString(final Instant instant, final String DATE_FORMAT) {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        ZoneId zoneId = ZoneId.of(ConstantConfig.TIMEZONE);
        DateTimeFormatter formatter = builder.appendPattern(DATE_FORMAT).toFormatter();
        return formatter.format(ZonedDateTime.ofInstant(instant, zoneId).toLocalDateTime());
    }
}
