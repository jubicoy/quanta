package fi.jubic.quanta.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;

@EasyValue
@JsonDeserialize(builder = DataRequest.Builder.class)
public abstract class DataRequest {

    @Nullable
    public abstract Long getOffset();

    @Nullable
    public abstract Long getLimit();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_DataRequest.Builder {

    }

}
