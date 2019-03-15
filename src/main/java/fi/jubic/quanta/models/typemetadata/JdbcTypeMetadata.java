package fi.jubic.quanta.models.typemetadata;

import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.external.importer.jdbc.Driver;

import java.util.List;

@EasyValue
public abstract class JdbcTypeMetadata {
    public abstract List<Driver> getDrivers();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_JdbcTypeMetadata.Builder {

    }
}
