package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easymapper.annotations.EasyId;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.db.tables.records.TagRecord;
import fi.jubic.quanta.exception.InputException;

import java.util.Set;
import java.util.regex.Pattern;

import static fi.jubic.quanta.db.tables.Tag.TAG;

@EasyValue
@JsonDeserialize(builder = Tag.Builder.class)
public abstract class Tag {
    private static final String NAMING_PATTERN_REGEX = "^[a-zA-Z0-9-_]+$";

    @EasyId
    public abstract Long getId();

    public abstract String getName();

    public abstract Builder toBuilder();


    public static Builder builder() {
        return new Builder();
    }

    public static String validate(String tag) {
        if (!Pattern.matches(NAMING_PATTERN_REGEX, tag)) {
            throw new InputException("Tag's name is invalid");
        }
        return tag;
    }

    public static Set<String> validate(Set<String> tags) {
        tags.forEach(Tag::validate);
        return tags;
    }

    public static class Builder extends EasyValue_Tag.Builder {
        @Override
        public Builder defaults(Builder builder) {
            return builder
                    .setId(0L)
                    .setName("");
        }

        @Override
        public Tag build() {
            validate(getName());
            return super.build();
        }
    }

    public static final TagRecordMapper<TagRecord> mapper = TagRecordMapper.builder(TAG)
            .setIdAccessor(TAG.ID)
            .setNameAccessor(TAG.NAME)
            .build();
}

