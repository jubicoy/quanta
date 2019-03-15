package fi.jubic.quanta.models.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.DataSeriesConfiguration;

import javax.annotation.Nullable;
import java.util.List;

@EasyValue
@JsonTypeName("CSV")
@JsonDeserialize(builder = CsvDataSeriesConfiguration.Builder.class)
public abstract class CsvDataSeriesConfiguration extends DataSeriesConfiguration {
    @Nullable
    public abstract List<String> getHeaders();

    public abstract Character getQuote();

    public abstract Character getDelimiter();

    public abstract String getSeparator();

    public abstract String getCharset();

    public abstract Builder toBuilder();

    @Override
    public <T> T visit(FunctionVisitor<T> visitor) {
        return visitor.onCsv(this);
    }

    @Override
    public void visit(ConsumerVisitor visitor) {
        visitor.onCsv(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_CsvDataSeriesConfiguration.Builder {
    }
}
