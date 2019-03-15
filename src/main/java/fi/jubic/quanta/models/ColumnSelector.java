package fi.jubic.quanta.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.InvocationColumnSelectorRecord;
import fi.jubic.quanta.db.tables.records.TaskColumnSelectorRecord;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;

import static fi.jubic.quanta.db.tables.InvocationColumnSelector.INVOCATION_COLUMN_SELECTOR;
import static fi.jubic.quanta.db.tables.TaskColumnSelector.TASK_COLUMN_SELECTOR;

@EasyValue
@JsonDeserialize(builder = ColumnSelector.Builder.class)
public abstract class ColumnSelector {
    @EasyId
    public abstract Long getId();

    public abstract Integer getColumnIndex();

    public abstract String getColumnName();

    public abstract Type getType();

    @Nullable
    public abstract TimeSeriesModifier getModifier();

    @Nullable
    public abstract WorkerDefColumn getWorkerDefColumn();

    @Nullable
    public abstract String getAlias();

    public abstract DataSeries getSeries();

    @JsonIgnore
    public boolean getIsGrouping() {
        return Objects.equals(
                getModifier(),
                TimeSeriesModifier.group_by);
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_ColumnSelector.Builder {
        @Override
        public Builder defaults(Builder builder) {
            Class<?> className = null;
            try {
                className = Class.forName("java.lang.String");
            }
            catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            }

            return builder
                    .setId(-1L)
                    .setColumnIndex(0)
                    .setColumnName("")
                    .setType(Type
                            .builder()
                            .setClassName(className)
                            .setFormat(null)
                            .setNullable(false)
                            .build()
                    )
                    .setModifier(null)
                    .setAlias("")
                    .setSeries(DataSeries.builder()
                            .setId(-1L)
                            .setName("")
                            .setDescription("")
                            .setTableName("")
                            .setColumns(Collections.emptyList())
                            .setDataConnection(null)
                            .build()
                    )
                    .setWorkerDefColumn(null);
        }
    }

    public static final ColumnSelectorRecordMapper<TaskColumnSelectorRecord>
            taskColumnSelectorMapper = ColumnSelectorRecordMapper.builder(TASK_COLUMN_SELECTOR)
                    .setIdAccessor(TASK_COLUMN_SELECTOR.ID)
                    .setColumnIndexAccessor(TASK_COLUMN_SELECTOR.COLUMN_INDEX)
                    .setColumnNameAccessor(TASK_COLUMN_SELECTOR.COLUMN_NAME)
                    .setTypeAccessor(new Type.TypeAccessor<>(
                            TASK_COLUMN_SELECTOR.CLASS,
                            TASK_COLUMN_SELECTOR.FORMAT,
                            TASK_COLUMN_SELECTOR.NULLABLE
                    ))
                    .setModifierAccessor(
                            TASK_COLUMN_SELECTOR.MODIFIER,
                            TimeSeriesModifier::toString,
                            TimeSeriesModifier::parse
                    )
                    .setAliasAccessor(TASK_COLUMN_SELECTOR.ALIAS)
                    .setSeriesAccessor(
                            TASK_COLUMN_SELECTOR.DATA_SERIES_ID,
                            DataSeries::getId
                    )
                    .setWorkerDefColumnAccessor(
                            TASK_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID,
                            WorkerDefColumn::getId
                    )
                    .build();

    public static final ColumnSelectorRecordMapper<InvocationColumnSelectorRecord>
            invocationColumnSelectorMapper = ColumnSelectorRecordMapper
                .builder(INVOCATION_COLUMN_SELECTOR)
                    .setIdAccessor(INVOCATION_COLUMN_SELECTOR.ID)
                    .setColumnIndexAccessor(INVOCATION_COLUMN_SELECTOR.COLUMN_INDEX)
                    .setColumnNameAccessor(INVOCATION_COLUMN_SELECTOR.COLUMN_NAME)
                    .setTypeAccessor(new Type.TypeAccessor<>(
                            INVOCATION_COLUMN_SELECTOR.CLASS,
                            INVOCATION_COLUMN_SELECTOR.FORMAT,
                            INVOCATION_COLUMN_SELECTOR.NULLABLE
                    ))
                    .setModifierAccessor(
                            INVOCATION_COLUMN_SELECTOR.MODIFIER,
                            TimeSeriesModifier::toString,
                            TimeSeriesModifier::parse
                    )
                    .setAliasAccessor(INVOCATION_COLUMN_SELECTOR.ALIAS)
                    .setSeriesAccessor(
                            INVOCATION_COLUMN_SELECTOR.DATA_SERIES_ID,
                            DataSeries::getId
                    )
                    .setWorkerDefColumnAccessor(
                            INVOCATION_COLUMN_SELECTOR.WORKER_DEFINITION_COLUMN_ID,
                            WorkerDefColumn::getId
                    )
                    .build();
}
