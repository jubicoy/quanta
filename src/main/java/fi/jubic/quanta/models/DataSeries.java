package fi.jubic.quanta.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.DataSeriesRecord;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static fi.jubic.quanta.db.tables.DataSeries.DATA_SERIES;

@EasyValue
@JsonDeserialize(builder = DataSeries.Builder.class)
public abstract class DataSeries {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract String getTableName();

    @Nullable
    public abstract Instant getDeletedAt();

    public abstract DataConnectionType getType();

    public abstract DataSeriesConfiguration getConfiguration();

    @Nullable
    public abstract DataConnection getDataConnection();

    public abstract List<Column> getColumns();

    public DataSeries validate() {
        if (
                getColumns()
                        .stream()
                        .noneMatch(
                                column -> column.getName().equals("time")
                        )
        ) {
            throw new IllegalArgumentException("Time column is missing");
        }

        if (
                !getColumns()
                .stream()
                .filter(column -> column.getName().equals("time"))
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .getIndex()
                .equals(0)
        ) {
            throw new IllegalArgumentException("Time column has wrong index, must be '0'");
        }

        return this;
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataSeries.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder.setId(0L)
                    .setName("")
                    .setDescription("")
                    .setDeletedAt(null)
                    .setTableName("")
                    .setDataConnection(null)
                    .setType(DataConnectionType.CSV)
                    .setConfiguration(
                            CsvDataSeriesConfiguration.builder()
                                    .setCharset("UTF-8")
                                    .setQuote('"')
                                    .setDelimiter(',')
                                    .setSeparator("\\r\\n")
                                    .build()
                    )
                    .setColumns(Collections.emptyList());
        }
    }

    public static final DataSeriesRecordMapper<DataSeriesRecord> mapper = DataSeriesRecordMapper
            .builder(DATA_SERIES)
            .setIdAccessor(DATA_SERIES.ID)
            .setNameAccessor(DATA_SERIES.NAME)
            .setDescriptionAccessor(DATA_SERIES.DESCRIPTION)
            .setTableNameAccessor(DATA_SERIES.TABLE_NAME)
            .setDeletedAtAccessor(DATA_SERIES.DELETED_AT, Timestamp::from, Timestamp::toInstant)
            .setTypeAccessor(
                    DATA_SERIES.TYPE,
                    DataConnectionType::name,
                    DataConnectionType::valueOf
            )
            .setConfigurationAccessor(
                    DATA_SERIES.CONFIGURATION,
                    DataSeries::writeConfig,
                    DataSeries::readConfig
            )
            .setDataConnectionAccessor(
                    DATA_SERIES.DATA_CONNECTION_ID, DataConnection::getId
            )
            .build();

    private static final ObjectMapper configMapper = new ObjectMapper();

    private static String writeConfig(DataSeriesConfiguration configuration) {
        try {
            return configMapper.writeValueAsString(configuration);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static DataSeriesConfiguration readConfig(String string) {
        try {
            return configMapper.readValue(string, DataSeriesConfiguration.class);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
