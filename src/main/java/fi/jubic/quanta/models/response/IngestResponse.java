package fi.jubic.quanta.models.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

@EasyValue
@JsonDeserialize(builder = IngestResponse.Builder.class)
public abstract class IngestResponse {
    public abstract Long getUpdatedRows();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_IngestResponse.Builder {

    }
}
