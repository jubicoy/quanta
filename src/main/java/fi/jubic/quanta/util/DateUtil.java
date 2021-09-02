package fi.jubic.quanta.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DateUtil {
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    public static DateTimeFormatter dateTimeFormatter(String format) {
        if (Objects.isNull(format)) {
            return DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
                    .withZone(ZoneId.from(ZoneOffset.UTC));
        }

        return DateTimeFormatter
                .ofPattern(format)
                .withZone(ZoneId.from(ZoneOffset.UTC));
    }
}
