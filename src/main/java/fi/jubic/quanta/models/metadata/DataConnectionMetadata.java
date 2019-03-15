package fi.jubic.quanta.models.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionType;

import javax.annotation.Nullable;

@EasyValue
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class DataConnectionMetadata {
    public abstract DataConnectionType getType();

    @Nullable
    public abstract JdbcDataConnectionMetadata getJdbcDataConnectionMetadata();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataConnectionMetadata.Builder {

    }
}
