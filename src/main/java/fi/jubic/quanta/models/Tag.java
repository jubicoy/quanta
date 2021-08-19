package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.TagRecord;

import static fi.jubic.quanta.db.tables.Tag.TAG;

@EasyValue
@JsonDeserialize(builder = Tag.Builder.class)
public abstract class Tag {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract Builder toBuilder();


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Tag.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setId(0L)
                    .setName("");
        }
    }

    public static final TagRecordMapper<TagRecord> mapper = TagRecordMapper.builder(TAG)
            .setIdAccessor(TAG.ID)
            .setNameAccessor(TAG.NAME)
            .build();
}

