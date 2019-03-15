package fi.jubic.quanta.models.metadata;

import fi.jubic.easyvalue.EasyValue;

import java.util.List;

@EasyValue
public abstract class JdbcDataConnectionMetadata {
    public abstract List<String> getTables();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_JdbcDataConnectionMetadata.Builder {

    }
}
