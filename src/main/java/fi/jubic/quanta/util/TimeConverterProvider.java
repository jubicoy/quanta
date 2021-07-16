package fi.jubic.quanta.util;

import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;
import org.jooq.ConverterProvider;
import org.jooq.impl.DefaultConverterProvider;

import java.time.Instant;

public class TimeConverterProvider implements org.jooq.ConverterProvider {
    final ConverterProvider delegate = new DefaultConverterProvider();
    private final TimeConverter timeConverter;

    public TimeConverterProvider() {
        this.timeConverter = new TimeConverter();
    }

    @Override
    public @Nullable <T, U> Converter<T, U> provide(Class<T> t, Class<U> u) {
        if (u == Instant.class) {
            return (Converter<T, U>) timeConverter;
        }
        // Delegate all other type pairs to jOOQ's default
        else {
            return delegate.provide(t, u);
        }
    }

}
