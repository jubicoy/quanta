package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.WorkerRecord;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;

import static fi.jubic.quanta.db.tables.Worker.WORKER;

@EasyValue
@JsonDeserialize(builder = Worker.Builder.class)
public abstract class Worker {
    @EasyId
    public abstract Long getId();

    public abstract WorkerDef getDefinition();

    public abstract String getToken();

    @JsonIgnore
    public WorkerStatus getStatus() {
        return getAcceptedOn() != null
                ? WorkerStatus.Accepted
                : WorkerStatus.Pending;
    }

    @Nullable
    public abstract Instant getAcceptedOn();

    @Nullable
    public abstract Instant getLastSeen();

    @Nullable
    public abstract Instant getDeletedAt();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Worker.Builder {
    }

    public static final WorkerRecordMapper<WorkerRecord> mapper = WorkerRecordMapper.builder(WORKER)
            .setIdAccessor(WORKER.ID)
            .setDefinitionAccessor(WORKER.DEFINITION_ID, WorkerDef::getId)
            .setTokenAccessor(WORKER.TOKEN)
            .setAcceptedOnAccessor(WORKER.ACCEPTED_ON, Timestamp::from, Timestamp::toInstant)
            .setLastSeenAccessor(WORKER.LAST_SEEN, Timestamp::from, Timestamp::toInstant)
            .setDeletedAtAccessor(WORKER.DELETED_AT, Timestamp::from, Timestamp::toInstant)
            .build();
}
