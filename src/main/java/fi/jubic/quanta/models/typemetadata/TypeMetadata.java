package fi.jubic.quanta.models.typemetadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionType;

import javax.annotation.Nullable;

@EasyValue
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class TypeMetadata {
    public abstract DataConnectionType getType();

    @Nullable
    public abstract CsvTypeMetadata getCsvTypeMetadata();

    @Nullable
    public abstract JdbcTypeMetadata getJdbcTypeMetadata();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TypeMetadata.Builder {

    }
}
