package fi.jubic.quanta.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nullable;
import javax.ws.rs.QueryParam;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @SuppressFBWarnings(
            value = "REDOS",
            justification = "Alternatives are exclusive and quantifiers are not used on same group."
    )
    public List<AnomalyQuerySelector> parseAnomalySelector() {
        // Return list of all selectors from query
        Pattern pattern = Pattern.compile(
                // G1: (Optional) Modifiers(grouping/aggregation)
                "^(?:(avg|min|max|sum|count|group_by|where|distinct)\\()?"
                        // G2: Type
                        + "(anomaly):"
                        // G3: Series name
                        + "([a-zA-Z0-9-_]+)"
                        // G4: (Optional) InvocationNumber/"latest"
                        + "(?:\\.([1-9]+|latest))?"
                        // G5: (Optional) Column name
                        + "(?:\\.([a-zA-Z0-9-_>\\s']+))?"
                        // G6: (Optional) Filter string
                        + "((?:\\s)?(?:=|!=|>|>=|<|<=)(?:\\s)?'[^'()]+')?\\)?"
                        // G7: (Optional) Alias
                        + "(?: as ([a-zA-Z0-9-_]+))?$"
        );

        List<Matcher> matchers = getSelectors()
                .stream()
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .collect(Collectors.toList());

        if (matchers.size() <= 0) {
            return Collections.emptyList();
        }

        return matchers.stream()
                .map(matcher -> {
                    TimeSeriesModifier modifier = Optional
                            .ofNullable(
                                    matcher.group(1)
                            )
                            .map(group ->
                                    Enum.valueOf(
                                            TimeSeriesModifier.class, group
                                    )
                            )
                            .orElse(null);

                    String filterCondition = matcher.group(6);

                    if (
                            Objects.nonNull(modifier)
                                    && modifier.equals(TimeSeriesModifier.where)
                                    && (
                                    Objects.isNull(filterCondition)
                                            || filterCondition.length() <= 0
                            )
                    ) {
                        throw new IllegalArgumentException(
                                "Filter selector is missing condition"
                        );
                    }

                    return AnomalyQuerySelector.builder()
                            .setModifier(modifier)
                            .setFilterCondition(filterCondition)
                            .setAnomalyResultSelector(
                                    AnomalyResultSelector.builder()
                                            .setTaskName(matcher.group(3))
                                            .setIsLatest(matcher.group(4).equals("latest"))
                                            .setInvocationNumber(
                                                    matcher.group(4).equals("latest")
                                                            ? -1L
                                                            : Long.parseLong(matcher.group(4))
                                            )
                                            .setColumnName(
                                                    matcher.group(5) != null
                                                            ? matcher.group(5)
                                                            : "*"
                                            )
                                            .build()
                            )
                            .build();
                })
                .collect(Collectors.toList());
    }
}
