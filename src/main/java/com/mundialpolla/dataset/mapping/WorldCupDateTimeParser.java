package com.mundialpolla.dataset.mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class WorldCupDateTimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{2}:\\d{2})\\s+UTC([+-]\\d{1,2})$");

    public Instant parse(String date, String time) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            Matcher matcher = TIME_PATTERN.matcher(time == null ? "" : time.trim());
            if (!matcher.matches()) {
                throw new IllegalArgumentException();
            }

            LocalTime localTime = LocalTime.parse(matcher.group(1));
            ZoneOffset offset = ZoneOffset.ofHours(Integer.parseInt(matcher.group(2)));
            return OffsetDateTime.of(localDate, localTime, offset).toInstant();
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid match date/time: date=" + date + ", time=" + time, exception);
        }
    }
}
