package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.SeriesTableRecord;
import fi.jubic.quanta.util.DateUtil;

import javax.annotation.Nullable;
import java.time.Instant;

import static fi.jubic.quanta.db.Tables.SERIES_TABLE;

@EasyValue
@JsonDeserialize(builder = SeriesTable.Builder.class)
public abstract class SeriesTable {
    @EasyId
    public abstract Long getId();

    public abstract String getTableName();

    public abstract DataSeries getDataSeries();

    @Nullable
    public abstract Instant getDeleteAt();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_SeriesTable.Builder {
    }

    public static final SeriesTableRecordMapper<SeriesTableRecord> mapper
            = SeriesTableRecordMapper.builder(SERIES_TABLE)
            .setIdAccessor(SERIES_TABLE.ID)
            .setTableNameAccessor(SERIES_TABLE.TABLE_NAME)
            .setDataSeriesAccessor(SERIES_TABLE.DATA_SERIES_ID, DataSeries::getId)
            .setDeleteAtAccessor(
                    SERIES_TABLE.DELETE_AT,
                    DateUtil::toLocalDateTime,
                    DateUtil::toInstant
            )
            .build();
}
