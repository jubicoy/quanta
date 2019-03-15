package fi.jubic.quanta.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

@EasyValue
@JsonDeserialize(builder = IngestSampleRequest.Builder.class)
public abstract class IngestSampleRequest {

    public abstract Object getJsonDocument();

    public abstract DataSeries getDataSeries();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_IngestSampleRequest.Builder {

    }
}
