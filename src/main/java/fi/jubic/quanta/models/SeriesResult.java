package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.SeriesResultRecord;

import javax.annotation.Nullable;

import static fi.jubic.quanta.db.tables.SeriesResult.SERIES_RESULT;

@EasyValue
@JsonDeserialize(builder = SeriesResult.Builder.class)
public abstract class SeriesResult {
    @EasyId
    public abstract Long getId();

    @Nullable
    public abstract Invocation getInvocation();

    public abstract String getTableName();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_SeriesResult.Builder {

    }

    public static final SeriesResultRecordMapper<SeriesResultRecord> mapper
            = SeriesResultRecordMapper.builder(SERIES_RESULT)
            .setIdAccessor(SERIES_RESULT.ID)
            .setInvocationAccessor(SERIES_RESULT.INVOCATION_ID, Invocation::getId)
            .setTableNameAccessor(SERIES_RESULT.TABLE_NAME)
            .build();
}
