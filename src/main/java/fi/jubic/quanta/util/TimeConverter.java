package fi.jubic.quanta.util;

import org.jooq.Converter;

import java.sql.Timestamp;
import java.time.Instant;

public class TimeConverter implements Converter<Timestamp, Instant> {

    @Override
    public Instant from(Timestamp timestamp) {
        return timestamp.toInstant();
    }

    @Override
    public Timestamp to(Instant instant) {
        return Timestamp.from(instant);
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Instant> toType() {
        return Instant.class;
    }
}
