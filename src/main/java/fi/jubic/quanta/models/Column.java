package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.ColumnRecord;

import javax.annotation.Nullable;

import static fi.jubic.quanta.db.tables.Column.COLUMN;

@EasyValue
@JsonDeserialize(builder = Column.Builder.class)
public abstract class Column {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract Type getType();

    @Nullable
    public abstract DataSeries getSeries();

    public abstract Integer getIndex();

    public abstract Builder toBuilder();

    public static Column of(Type type, Long seriesId) {
        return Column.builder()
                .setId(0L)
                .setName("")
                .setType(type)
                .setSeries(
                        DataSeries.builder()
                                .setId(seriesId)
                                .setName("")
                                .setDescription("")
                                .setTableName("")
                                .setDataConnection(null)
                                .build()
                )
                .setIndex(0)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Column.Builder {

    }

    public static final ColumnRecordMapper<ColumnRecord> seriesColumnMapper
            = ColumnRecordMapper.builder(COLUMN)
            .setIdAccessor(COLUMN.ID)
            .setNameAccessor(COLUMN.NAME)
            .setSeriesAccessor(COLUMN.DATA_SERIES_ID, DataSeries::getId)
            .setTypeAccessor(new Type.TypeAccessor<>(
                    COLUMN.CLASS,
                    COLUMN.FORMAT,
                    COLUMN.NULLABLE
            ))
            .setIndexAccessor(COLUMN.INDEX)
            .build();
}
