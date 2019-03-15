package fi.jubic.quanta.models.request.grafana;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EasyValue
@JsonDeserialize(builder = GrafanaQueryRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GrafanaQueryRequest {
    public abstract GrafanaQueryRange getRange();

    public abstract String getInterval();

    public abstract Long getIntervalMs();

    public abstract List<Map<String, String>> getTargets();

    @Nullable
    public abstract String getFormat();

    public abstract Integer getMaxDataPoints();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_GrafanaQueryRequest.Builder {

    }

}
