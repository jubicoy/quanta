package fi.jubic.quanta.models.response.grafana;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.util.List;

@EasyValue
@JsonDeserialize(builder = GrafanaQueryResponse.Builder.class)
public abstract class GrafanaQueryResponse {
    public abstract String getTarget();

    public abstract List<List<Object>> getDatapoints();

    public abstract GrafanaQueryResponse.Builder toBuilder();

    public static GrafanaQueryResponse.Builder builder() {
        return new GrafanaQueryResponse.Builder();
    }

    public static class Builder extends EasyValue_GrafanaQueryResponse.Builder {

    }
}
