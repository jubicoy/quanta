package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.TagDataconnectionRecord;
import fi.jubic.quanta.db.tables.records.TagTaskRecord;

import static fi.jubic.quanta.db.Tables.TAG_DATACONNECTION;
import static fi.jubic.quanta.db.Tables.TAG_TASK;

@EasyValue
@JsonDeserialize(builder = TagAssignment.Builder.class)
public abstract class TagAssignment {
    @EasyId
    public abstract Long getId();

    public abstract Long getParentId();

    public abstract Tag getTag();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_TagAssignment.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setId(0L);
        }
    }

    public static final TagAssignmentRecordMapper<TagTaskRecord> tagTaskMapper
            = TagAssignmentRecordMapper
            .builder(TAG_TASK)
            .setIdAccessor(TAG_TASK.TAG_ID)
            .setParentIdAccessor(TAG_TASK.TASK_ID)
            .setTagAccessor(TAG_TASK.TAG_ID, Tag::getId)
            .build();

    public static final TagAssignmentRecordMapper<TagDataconnectionRecord> tagDataConnectionMapper
             = TagAssignmentRecordMapper
            .builder(TAG_DATACONNECTION)
            .setTagAccessor(TAG_DATACONNECTION.TAG_ID, Tag::getId)
            .setIdAccessor(TAG_DATACONNECTION.TAG_ID)
            .setParentIdAccessor(TAG_DATACONNECTION.DATACONNECTION_ID)
            .build();
}

