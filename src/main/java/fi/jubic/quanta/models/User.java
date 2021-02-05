package fi.jubic.quanta.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.UserRecord;
import fi.jubic.snoozy.auth.UserPrincipal;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;

import static fi.jubic.quanta.db.Tables.USER;

@EasyValue
@JsonDeserialize(builder = User.Builder.class)
public abstract class User implements UserPrincipal {
    @EasyId
    public abstract Long getId();

    public abstract String getRole();

    public abstract String getName();

    @JsonIgnore
    public abstract String getPasswordHash();

    @JsonIgnore
    public abstract String getSalt();

    @JsonIgnore
    @Nullable
    public abstract Instant getCreationDate();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_User.Builder {
    }

    public static final UserRecordMapper<UserRecord> mapper =
            UserRecordMapper.builder(USER)
                    .setIdAccessor(USER.ID)
                    .setRoleAccessor(USER.ROLE)
                    .setNameAccessor(USER.NAME)
                    .setPasswordHashAccessor(USER.PASSWORD_HASH)
                    .setSaltAccessor(USER.SALT)
                    .setCreationDateAccessor(USER.CREATION_DATE,
                            Timestamp::from,
                            Timestamp::toInstant
                    )
                    .build();
}
