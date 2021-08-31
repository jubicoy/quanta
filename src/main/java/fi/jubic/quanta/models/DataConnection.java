package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.DataConnectionRecord;
import fi.jubic.quanta.models.configuration.ImportWorkerDataConnectionConfiguration;
import fi.jubic.quanta.util.DateUtil;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static fi.jubic.quanta.db.tables.DataConnection.DATA_CONNECTION;

@EasyValue
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = DataConnection.Builder.class)
public abstract class DataConnection {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract String getDescription();

    public abstract DataConnectionType getType();

    public abstract DataConnectionConfiguration getConfiguration();

    @Nullable
    public abstract List<DataSeries> getSeries();

    @Nullable
    public abstract Instant getDeletedAt();

    @Nullable
    public abstract List<String> getTags();

    public abstract Builder toBuilder();

    @JsonIgnore
    public ImportWorkerDataConnectionConfiguration getImportWorkerConfiguration() {
        return getConfiguration().visit(
                new DataConnectionConfiguration.DefaultFunctionVisitor<>() {
                    @Override
                    public ImportWorkerDataConnectionConfiguration onImportWorker(
                            ImportWorkerDataConnectionConfiguration importWorkerConfiguration
                    ) {
                        return importWorkerConfiguration;
                    }

                    @Override
                    public ImportWorkerDataConnectionConfiguration otherwise(
                            DataConnectionConfiguration configuration
                    ) {
                        throw new IllegalStateException("Unexpected configuration type");
                    }
                }
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataConnection.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setId(0L)
                    .setName("")
                    .setDescription("")
                    .setDeletedAt(null)
                    .setSeries(Collections.emptyList())
                    .setTags(Collections.emptyList());
        }
    }

    public static final DataConnectionRecordMapper<DataConnectionRecord> mapper
            = DataConnectionRecordMapper.builder(DATA_CONNECTION)
            .setIdAccessor(DATA_CONNECTION.ID)
            .setNameAccessor(DATA_CONNECTION.NAME)
            .setDescriptionAccessor(DATA_CONNECTION.DESCRIPTION)
            .setTypeAccessor(
                    DATA_CONNECTION.TYPE,
                    DataConnectionType::name,
                    DataConnectionType::valueOf
            )
            .setConfigurationAccessor(
                    DATA_CONNECTION.CONFIGURATION,
                    DataConnection::writeConfig,
                    DataConnection::readConfig
            )
            .setDeletedAtAccessor(
                    DATA_CONNECTION.DELETED_AT,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .build();

    private static final ObjectMapper configMapper = new ObjectMapper();

    private static String writeConfig(DataConnectionConfiguration configuration) {
        try {
            return configMapper.writeValueAsString(configuration);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static DataConnectionConfiguration readConfig(String string) {
        try {
            return configMapper.readValue(string, DataConnectionConfiguration.class);
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
