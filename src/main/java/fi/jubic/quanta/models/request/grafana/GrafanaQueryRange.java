package fi.jubic.quanta.models.request.grafana;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public class GrafanaQueryRange {
    @JsonProperty
    private Instant from;
    @JsonProperty
    private Instant to;
    @JsonProperty
    private Map<String, String> raw;

    public GrafanaQueryRange() {
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }

    public Map<String, String> getRaw() {
        return raw;
    }

    public void setRaw(Map<String, String> raw) {
        this.raw = raw;
    }
}
