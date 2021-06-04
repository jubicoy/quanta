package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataSeriesConfiguration;

@EasyValue
@JsonTypeName("JDBC")
@JsonDeserialize(builder = JdbcDataSeriesConfiguration.Builder.class)
public abstract class JdbcDataSeriesConfiguration extends DataSeriesConfiguration {
    public abstract String getQuery();

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

    public static class Builder extends EasyValue_JdbcDataSeriesConfiguration.Builder {

    }
}
