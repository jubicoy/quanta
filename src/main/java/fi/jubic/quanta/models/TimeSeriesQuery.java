package fi.jubic.quanta.models;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class TimeSeriesQuery {
    @QueryParam("start")
    private Instant start;

    @QueryParam("end")
    private Instant end;

    @QueryParam("selectors")
    private List<String> selectors;

    @QueryParam("interval")
    @Nullable
    private Long intervalSeconds;

    public TimeSeriesQuery() {

    }

    private TimeSeriesQuery(
            Instant start,
            Instant end,
            @Nullable List<String> selectors,
            @Nullable Long intervalSeconds
    ) {
        this.start = start;
        this.end = end;
        this.selectors = selectors;
        this.intervalSeconds = intervalSeconds;
    }

    public TimeSeriesQuery withStart(Instant start) {
        return new TimeSeriesQuery(
                start,
                this.end,
                this.selectors,
                this.intervalSeconds
        );
    }

    public TimeSeriesQuery withEnd(Instant end) {
        return new TimeSeriesQuery(
                this.start,
                end,
                this.selectors,
                this.intervalSeconds
        );
    }

    public TimeSeriesQuery withFilters(List<String> filters) {
        return new TimeSeriesQuery(
                this.start,
                this.end,
                filters,
                this.intervalSeconds
        );
    }

    public TimeSeriesQuery withIntervalSeconds(Long intervalSeconds) {
        return new TimeSeriesQuery(
                this.start,
                this.end,
                this.selectors,
                intervalSeconds
        );
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public List<String> getSelectors() {
        return selectors;
    }

    public Long getIntervalSeconds() {
        return Optional.ofNullable(intervalSeconds)
                .orElse(0L);
    }
}
