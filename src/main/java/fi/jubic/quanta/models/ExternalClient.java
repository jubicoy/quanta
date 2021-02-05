package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.ExternalClientRecord;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;

import static fi.jubic.quanta.db.tables.ExternalClient.EXTERNAL_CLIENT;

@EasyValue
@JsonDeserialize(builder = ExternalClient.Builder.class)
public abstract class ExternalClient {
    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract String getToken();

    public abstract String getDescription();

    public abstract User getUser();

    @Nullable
    public abstract Task getTask();

    @Nullable
    public abstract Instant getDeletedAt();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_ExternalClient.Builder {
    }

    public static final ExternalClientRecordMapper<ExternalClientRecord> mapper =
            ExternalClientRecordMapper.builder(EXTERNAL_CLIENT)
                    .setIdAccessor(EXTERNAL_CLIENT.ID)
                    .setNameAccessor(EXTERNAL_CLIENT.NAME)
                    .setTokenAccessor(EXTERNAL_CLIENT.TOKEN)
                    .setDescriptionAccessor(EXTERNAL_CLIENT.DESCRIPTION)
                    .setUserAccessor(EXTERNAL_CLIENT.USER_ID, User::getId)
                    .setTaskAccessor(EXTERNAL_CLIENT.TASK_ID, Task::getId)
                    .setDeletedAtAccessor(EXTERNAL_CLIENT.DELETED_AT,
                        Timestamp::from,
                        Timestamp::toInstant
                    )
                    .build();
}
