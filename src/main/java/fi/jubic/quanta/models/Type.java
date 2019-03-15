package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.MappingException;
import fi.jubic.easymapper.jooq.JooqFieldAccessor;
import fi.jubic.easymapper.jooq.PlainJooqFieldAccessor;
import fi.jubic.easymapper.jooq.TransformingJooqFieldAccessor;
import fi.jubic.easyvalue.EasyValue;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import javax.annotation.Nullable;

@EasyValue
@JsonDeserialize(builder = Type.Builder.class)
public abstract class Type {

    @JsonProperty("className")
    public abstract Class<?> getClassName();

    @Nullable
    public abstract String getFormat();

    public abstract boolean isNullable();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Type.Builder {

    }

    public static class TypeAccessor<R extends Record> implements JooqFieldAccessor<R, Type> {
        private final JooqFieldAccessor<R, Class<?>> classAccessor;
        private final JooqFieldAccessor<R, String> formatAccessor;
        private final JooqFieldAccessor<R, Boolean> nullableAccessor;

        private TypeAccessor(
                JooqFieldAccessor<R, Class<?>> classAccessor,
                JooqFieldAccessor<R, String> formatAccessor,
                JooqFieldAccessor<R, Boolean> nullableAccessor
        ) {

            this.classAccessor = classAccessor;
            this.formatAccessor = formatAccessor;
            this.nullableAccessor = nullableAccessor;
        }

        public TypeAccessor(
                TableField<R, String> classField,
                TableField<R, String> formatField,
                TableField<R, Boolean> nullableField
        ) {
            this.classAccessor = new TransformingJooqFieldAccessor<>(
                    classField,
                    Class::getCanonicalName,
                    className -> {
                        try {
                            return Class.forName(className);
                        }
                        catch (ClassNotFoundException exception) {
                            throw new MappingException(exception);
                        }
                    }
            );
            this.formatAccessor = new PlainJooqFieldAccessor<>(formatField);
            this.nullableAccessor = new PlainJooqFieldAccessor<>(nullableField);
        }


        @Override
        public JooqFieldAccessor<R, Type> alias(Table<R> tableAlias) {
            return new TypeAccessor<>(
                    classAccessor.alias(tableAlias),
                    formatAccessor.alias(tableAlias),
                    nullableAccessor.alias(tableAlias)
            );
        }

        @Override
        public Type extract(R input) throws MappingException {
            return Type.builder()
                    .setClassName(classAccessor.extract(input))
                    .setFormat(formatAccessor.extract(input))
                    .setNullable(nullableAccessor.extract(input))
                    .build();
        }

        @Override
        public R write(R output, Type value) throws MappingException {
            output = classAccessor.write(output, value.getClassName());
            output = formatAccessor.write(output, value.getFormat());
            output = nullableAccessor.write(output, value.isNullable());
            return output;
        }
    }
}
