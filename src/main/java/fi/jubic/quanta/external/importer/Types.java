package fi.jubic.quanta.external.importer;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Types {
    private static Stream<String> getKnownDateTimePatterns() {
        return Stream.of(
            "yyyy-MM-dd HH:mm:ss Z 'UTC'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "hhmmssffffff",
            "hhmmss+|-hhmm",
            "hhmmssZ",
            "hhmmss+|-hhmm",
            "yyyymmddThhmmssffffff",
            "yyyymmddThhmmss+|-hhmm",
            "yyyymmddThhmmssZ",
            "hh:mm:ss.ffffff",
            "hh:mm:ss.ffffff+|-hh:mm",
            "hh:mm:ss.ffffff+|-hh:mm",
            "yyyy-MM-ddThh:mm:ss.ffffff",
            "yyyy-MM-ddThh:mm:ss.nnnnnn+|-hh:mm"
        );
    }

    private static Stream<String> getKnownDatePatterns() {
        return Stream.of(
            "yyyymmdd",
            "yyyy-MM-dd",
            "yyyy-mm-dd"
        );
    }

    private String value;

    private Types(String value) {
        this.value = value;
    }

    public static Types of(String value) {
        return new Types(value);
    }

    public static List<Class<?>> getSupportedTypes() {
        return Stream.of(
                String.class,
                Boolean.class,
                Double.class,
                LocalDateTime.class,
                LocalDate.class,
                Long.class
        )
                .collect(Collectors.toList());
    }

    // Returns a map entry with class and (nullable) format string
    public Map.Entry<Class<?>, String> guess() {
        if (tryBoolean()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    Boolean.class,
                    null
            );
        }

        if (tryLong()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    Long.class,
                    null
            );
        }

        if (tryDouble()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    Double.class,
                    null
            );
        }

        Optional<String> dateTimePatternOptional = tryDateTime();

        if (dateTimePatternOptional.isPresent()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    LocalDateTime.class,
                    dateTimePatternOptional.get()
            );
        }

        Optional<String> datePatternOptional = tryDate();

        if (datePatternOptional.isPresent()) {
            return new AbstractMap.SimpleImmutableEntry<>(
                    LocalDate.class,
                    datePatternOptional.get()
            );
        }

        return new AbstractMap.SimpleImmutableEntry<>(
                String.class,
                null
        );
    }

    public static Optional<String> getSqlType(Class<?> className) {
        switch (className.getName()) {
            case "java.lang.String":
                return Optional.of("VARCHAR");
            case "java.lang.Boolean":
                return Optional.of("BOOLEAN");
            case "java.lang.Long":
                return Optional.of("BIGINT");
            case "java.lang.Double":
                return Optional.of("DOUBLE PRECISION");
            case "java.time.LocalDateTime":
                return Optional.of("TIMESTAMPTZ");
            case "java.time.LocalDate":
                return Optional.of("DATE");
            default:
                return Optional.empty();
        }
    }

    public static Object getValueAsType(
            Class<?> className,
            String value,
            @Nullable String format
    ) {
        switch (className.getName()) {
            case "java.lang.Boolean":
                return Boolean.parseBoolean(value);
            case "java.lang.Double":
                return Double.parseDouble(value);
            case "java.lang.Long":
                return Long.parseLong(value);
            case "java.lang.Integer":
                return Integer.parseInt(value);
            case "java.time.LocalDateTime":
                LocalDateTime dateTime;

                if (format != null) {
                    DateTimeFormatter pattern = DateTimeFormatter.ofPattern(format);
                    dateTime = LocalDateTime.parse(value, pattern);
                }
                else {
                    dateTime = LocalDateTime.parse(value);
                }

                return dateTime;
            case "java.time.LocalDate":
                LocalDate date;

                if (format != null) {
                    DateTimeFormatter pattern = DateTimeFormatter.ofPattern(format);
                    date = LocalDate.parse(value, pattern);
                }
                else {
                    date = LocalDate.parse(value);
                }

                return date;
            default:
                return value;
        }
    }

    public static Optional<String> getSqlValue(
            Class<?> className,
            String value,
            @Nullable String format
    ) {
        switch (className.getName()) {
            case "java.lang.String":
                return Optional.of(String.format("\"%s\"", value));
            case "java.lang.Boolean":
                return Optional.of(Boolean.parseBoolean(value) ? "1" : "0");
            case "java.lang.Long":
            case "java.lang.Integer":
            case "java.lang.Double":
                return Optional.of(value);
            case "java.time.LocalDateTime":
                Optional<LocalDateTime> dateTime;

                if (format != null) {
                    dateTime = Optional.of(format)
                            .map(DateTimeFormatter::ofPattern)
                            .map(f -> LocalDateTime.parse(value, f));
                }
                else {
                    dateTime = Optional.of(value)
                            .map(LocalDateTime::parse);
                }

                return dateTime.map(Timestamp::valueOf)
                        .map(Timestamp::toString)
                        .map(s -> String.format("'%s'", s));
            case "java.time.LocalDate":
                Optional<LocalDate> date;

                if (format != null) {
                    date = Optional.of(format)
                            .map(DateTimeFormatter::ofPattern)
                            .map(f -> LocalDate.parse(value, f));
                }
                else {
                    date = Optional.of(value)
                            .map(LocalDate::parse);
                }

                return date
                        .map(s -> String.format("'%s'", s));
            default:
                return Optional.empty();
        }
    }

    private boolean tryBoolean() {
        switch (value.toLowerCase()) {
            case "0":
            case "1":
            case "true":
            case "false":
                return true;
            default:
                return false;
        }
    }

    private boolean tryDouble() {
        Double d = null;

        try  {
            d = Double.parseDouble(value);
        }
        catch (Exception ignored) {
        }

        return d != null;
    }

    private boolean tryLong() {
        Long l = null;

        try {
            l = Long.parseLong(value);
        }
        catch (Exception ignored) {
        }

        return l != null;
    }

    private Optional<String> tryDateTime() {
        return getKnownDateTimePatterns()
                .filter(p -> isParseableDateTime.apply(p, value))
                .findFirst();
    }

    private Optional<String> tryDate() {
        return getKnownDatePatterns()
                .filter(p -> isParseableDate.apply(p, value))
                .findFirst();
    }

    private static BiFunction<String, String, Boolean> isParseableDateTime = (
            String pattern,
            String value
    ) -> {
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
        }
        catch (Exception ignored) {
        }

        return dateTime != null;
    };

    private static BiFunction<String, String, Boolean> isParseableDate = (
            String pattern,
            String value
    ) -> {
        LocalDate date = null;
        try {
            date = LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
        }
        catch (Exception ignored) {
        }

        return date != null;
    };
}
