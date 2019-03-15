package fi.jubic.quanta.models.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;
import fi.jubic.quanta.models.InvocationStatus;

@EasyValue
@JsonDeserialize(builder = UpdateStatusRequest.Builder.class)
public abstract class UpdateStatusRequest {

    public abstract InvocationStatus getStatus();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_UpdateStatusRequest.Builder {

    }
}
