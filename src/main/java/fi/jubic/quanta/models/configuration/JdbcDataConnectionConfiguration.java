package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataConnectionConfiguration;

@EasyValue
@JsonTypeName("JDBC")
@JsonDeserialize(builder = JdbcDataConnectionConfiguration.Builder.class)
public abstract class JdbcDataConnectionConfiguration extends DataConnectionConfiguration {
    public abstract String getDriverJar();

    public abstract String getDriverClass();

    public abstract String getConnectionString();

    public abstract String getUsername();

    public abstract String getPassword();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onJdbc(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onJdbc(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_JdbcDataConnectionConfiguration.Builder {

    }
}
