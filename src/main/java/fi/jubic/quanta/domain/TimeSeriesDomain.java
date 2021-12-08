package fi.jubic.quanta.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesColumnSelector;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Measurement;
import fi.jubic.quanta.models.OutputColumn;
import fi.jubic.quanta.models.QueryType;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.SeriesResultColumnSelector;
import fi.jubic.quanta.models.SeriesResultOutputColumnSelector;
import fi.jubic.quanta.models.SeriesResultSelector;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TimeSeriesColumnSelector;
import fi.jubic.quanta.models.TimeSeriesFilter;
import fi.jubic.quanta.models.TimeSeriesGroupSelector;
import fi.jubic.quanta.models.TimeSeriesModifier;
import fi.jubic.quanta.models.TimeSeriesQuery;
import fi.jubic.quanta.models.TimeSeriesQuerySelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputColumnSelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputFilter;
import fi.jubic.quanta.models.TimeSeriesResultOutputGroupSelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputSelector;
import fi.jubic.quanta.models.TimeSeriesSelector;
import fi.jubic.quanta.models.WorkerDefColumn;
import fi.jubic.quanta.models.WorkerDefColumnType;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class TimeSeriesDomain {
    @Inject
    TimeSeriesDomain() {

    }

    public List<TimeSeriesQuerySelector> parseSelectors(TimeSeriesQuery query) {
        List<Matcher> matchers = query.getSelectors()
                .stream()
                .map(TimeSeriesDomain::getMatcher)
                .filter(Matcher::matches)
                .collect(Collectors.toList());

        if (matchers.size() <= 0) {
            throw new IllegalArgumentException("Found no valid selectors.");
        }
        else {
            return matchers
                    .stream()
                    .map(matcher -> {
                        QueryType type = Enum.valueOf(
                                QueryType.class,
                                matcher.group(2)
                        );
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

                        TimeSeriesQuerySelector.Builder builder
                                = TimeSeriesQuerySelector.builder()
                                .setModifier(modifier)
                                .setFilterCondition(filterCondition)
                                .setAlias(matcher.group(7));
                        if (type.equals(QueryType.series)) {
                            return builder
                                    .setDataSeriesColumnSelector(
                                            DataSeriesColumnSelector.builder()
                                                    .setName(matcher.group(3))
                                                    .setColumnName(matcher.group(5))
                                                    .build()
                                    )
                                    .build();
                        }
                        else if (type.equals(QueryType.result)) {
                            return builder
                                    .setSeriesResultColumnSelector(
                                            SeriesResultColumnSelector.builder()
                                                    .setName(matcher.group(3))
                                                    .setIsLatest(matcher.group(4).equals("latest"))
                                                    .setInvocationNumber(matcher.group(4)
                                                            .equals("latest")
                                                            ? -1L
                                                            : Long.parseLong(matcher.group(4))
                                                    )
                                                    .setColumnName(matcher.group(5))
                                                    .build()
                                    )
                                    .build();
                        }
                        else {
                            return builder
                                    .setSeriesResultOutputColumnSelector(
                                            SeriesResultOutputColumnSelector.builder()
                                                    .setName(matcher.group(3))
                                                    .setIsLatest(matcher.group(4).equals("latest"))
                                                    .setInvocationNumber(matcher.group(4)
                                                            .equals("latest")
                                                            ? -1L
                                                            : Long.parseLong(matcher.group(4))
                                                    )
                                                    .setColumnName(matcher.group(5))
                                                    .build()
                                    )
                                    .build();
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    @SuppressFBWarnings(
            value = "REDOS",
            justification = "Alternatives are exclusive and quantifiers are not used on same group."
    )
    public static Matcher getMatcher(String selector) {
        // Return list of all selectors from query
        Pattern pattern = Pattern.compile(
                // G1: (Optional) Modifiers(grouping/aggregation)
                "^(?:(avg|min|max|sum|group_by|where|distinct|count)\\()?"
                        // G2: Type
                        + "(series|result|result_output):"
                        // G3: Series name
                        + "([a-zA-Z0-9-_]+)"
                        // G4: (Optional) InvocationNumber/"latest"
                        + "(?:\\.([1-9]+|latest))?"
                        // G5: Column name
                        + "\\.([a-zA-Z0-9-_]+)"
                        // G6: (Optional) Filter string
                        + "((?:\\s)?(?:=|!=|>|>=|<|<=)(?:\\s)?'[^'()]+')?\\)?"
                        // G7: (Optional) Alias
                        + "(?: as ([a-zA-Z0-9-_]+))?$"
        );
        return pattern.matcher(selector);
    }

    public List<TimeSeriesQuerySelector> getSelectorsLimitedToTask(
            List<TimeSeriesQuerySelector> selectors,
            Task allowedTask
    ) {
        return selectors
                .stream()
                .filter(
                        selector -> {
                            if (
                                    selector.getType().equals(QueryType.result)
                                    || selector.getType().equals(QueryType.result_output)
                            ) {
                                return selector
                                        .getSelector()
                                        .getName()
                                        .equals(
                                                allowedTask.getName()
                                        );
                            }
                            return false;
                        }
                )
                .collect(Collectors.toList());
    }

    public Map<String, List<TimeSeriesQuerySelector>> parseDataSeriesSelections(
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(selector -> selector.getType().equals(QueryType.series))
                .collect(
                        Collectors.groupingBy(
                                querySelector -> Objects
                                        .requireNonNull(
                                                querySelector.getDataSeriesColumnSelector()
                                        )
                                        .getName()
                        )
                );
    }

    public Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> parseSeriesResultsSelections(
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(selector -> selector.getType().equals(QueryType.result))
                .collect(
                        Collectors.groupingBy(
                                querySelector -> {
                                    SeriesResultColumnSelector select = Objects
                                            .requireNonNull(querySelector
                                                    .getSeriesResultColumnSelector()
                                            );
                                    return SeriesResultSelector.builder()
                                            .setTaskName(select.getName())
                                            .setIsLatest(select.getIsLatest())
                                            .setInvocationNumber(select.getInvocationNumber())
                                            .build();
                                }
                        )
                );
    }

    public Map<SeriesResultSelector, List<TimeSeriesQuerySelector>>
            parseSeriesResultOutputSelections(List<TimeSeriesQuerySelector> selectors) {
        return selectors
                .stream()
                .filter(selector -> selector.getType().equals(QueryType.result_output))
                .collect(
                        Collectors.groupingBy(
                                querySelector -> {
                                    SeriesResultOutputColumnSelector select = Objects
                                            .requireNonNull(
                                                    querySelector
                                                            .getSeriesResultOutputColumnSelector()
                                            );
                                    return SeriesResultSelector.builder()
                                            .setTaskName(select.getName())
                                            .setIsLatest(select.getIsLatest())
                                            .setInvocationNumber(select.getInvocationNumber())
                                            .build();
                                }
                        )
                );
    }

    public TimeSeriesSelector parseTimeSeriesSelectors(
            DataSeries dataSeries,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return TimeSeriesSelector.builder()
                .setDataSeries(dataSeries)
                .setColumnSelectors(
                        parseColumnSelectors(
                                dataSeries.getColumns(),
                                selectors
                        )
                )
                .setGroupings(
                        parseGroupings(
                                dataSeries.getColumns(),
                                selectors
                        )
                )
                .setFilters(
                        parseFilters(
                                dataSeries.getColumns(),
                                selectors
                        )
                )
                .build();
    }

    public TimeSeriesSelector parseTimeSeriesSelectors(
            SeriesResult seriesResult,
            DataSeries invocationDataSeries,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return TimeSeriesSelector.builder()
                .setSeriesResult(seriesResult)
                .setColumnSelectors(
                        parseColumnSelectors(
                                invocationDataSeries.getColumns(),
                                selectors
                        )
                )
                .setGroupings(
                        parseGroupings(
                                invocationDataSeries.getColumns(),
                                selectors
                        )
                )
                .setFilters(
                        parseFilters(
                                invocationDataSeries.getColumns(),
                                selectors
                        )
                )
                .build();
    }

    public TimeSeriesSelector parseTimeSeriesSelectors(
            SeriesResult seriesResult,
            Invocation invocation,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return TimeSeriesSelector.builder()
                .setSeriesResultOutput(seriesResult)
                .setResultOutputColumnSelectors(
                        parseResultOutputColumnSelectors(
                                invocation.getOutputColumns(),
                                selectors
                        )
                )
                .setResultOutputGroupings(
                        parseResultOutputGroupings(
                                invocation.getOutputColumns(),
                                selectors
                        )
                )
                .setResultOutputFilters(
                        parseResultOutputFilters(
                                invocation.getOutputColumns(),
                                selectors
                        )
                )
                .build();
    }

    private List<TimeSeriesColumnSelector> parseColumnSelectors(
            List<Column> columns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsDataSelector)
                .map(selector -> TimeSeriesColumnSelector.builder()
                        .setColumn(
                                columns
                                        .stream()
                                        .filter(column ->
                                                column.getName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Column not found")
                                        )
                        )
                        .setModifier(selector.getModifier())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesGroupSelector> parseGroupings(
            List<Column> columns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsGrouping)
                .map(selector -> TimeSeriesGroupSelector.builder()
                        .setColumn(
                                columns
                                        .stream()
                                        .filter(column ->
                                                column.getName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Column not found")
                                        )
                        )
                        .setAlias(selector.getAlias())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesFilter> parseFilters(
            List<Column> columns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsFilter)
                .map(selector -> TimeSeriesFilter.builder()
                        .setColumn(
                                columns
                                        .stream()
                                        .filter(column ->
                                                column.getName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Column not found")
                                        )
                        )
                        .setFilterCondition(selector.getFilterCondition())
                        .setFullFilterString(selector.toString())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesResultOutputColumnSelector> parseResultOutputColumnSelectors(
            List<OutputColumn> outputColumns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsDataSelector)
                .map(selector -> TimeSeriesResultOutputColumnSelector.builder()
                        .setOutputColumn(
                                outputColumns
                                        .stream()
                                        .filter(column ->
                                                column.getColumnName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Worker Def Column not found")
                                        )
                        )
                        .setModifier(selector.getModifier())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesResultOutputGroupSelector> parseResultOutputGroupings(
            List<OutputColumn> outputColumns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsGrouping)
                .map(selector -> TimeSeriesResultOutputGroupSelector.builder()
                        .setOutputColumn(
                                outputColumns
                                        .stream()
                                        .filter(column ->
                                                column.getColumnName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Worker Def Column not found")
                                        )
                        )
                        .setAlias(selector.getAlias())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesResultOutputFilter> parseResultOutputFilters(
            List<OutputColumn> outputColumns,
            List<TimeSeriesQuerySelector> selectors
    ) {
        return selectors
                .stream()
                .filter(TimeSeriesQuerySelector::getIsFilter)
                .map(selector -> TimeSeriesResultOutputFilter.builder()
                        .setOutputColumn(
                                outputColumns
                                        .stream()
                                        .filter(column ->
                                                column.getColumnName().equals(
                                                        selector.getSelector().getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Worker Def Column not found")
                                        )
                        )
                        .setFilterCondition(selector.getFilterCondition())
                        .setFullFilterString(selector.toString())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public TimeSeriesSelector parseTimeSeriesSelectorsWithInvocation(
            DataSeries invocationDataSeries,
            List<ColumnSelector> columnSelectors
    ) {
        return TimeSeriesSelector.builder()
                .setDataSeries(invocationDataSeries)
                .setColumnSelectors(
                        parseColumnSelectorsWithInvocation(
                                invocationDataSeries,
                                columnSelectors
                        )
                )
                .setGroupings(
                        parseGroupingsWithInvocation(
                                invocationDataSeries,
                                columnSelectors
                        )
                )
                .setFilters(
                        // Filters are not used here
                        Collections.emptyList()
                )
                .build();
    }

    private List<TimeSeriesColumnSelector> parseColumnSelectorsWithInvocation(
            DataSeries invocationDataSeries,
            List<ColumnSelector> columnSelectors
    ) {
        return columnSelectors
                .stream()
                .filter(columnSelector -> !columnSelector.getIsGrouping())
                .map(columnSelector -> TimeSeriesColumnSelector
                        .builder()
                        .setColumn(
                                invocationDataSeries.getColumns()
                                        .stream()
                                        .filter(column -> column.getName().equals(
                                                columnSelector.getColumnName()
                                                )
                                        )
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new NotFoundException(
                                                        "Column not found")
                                        )
                        )
                        .setModifier(columnSelector.getModifier())
                        .setAlias(columnSelector.getAlias())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<TimeSeriesGroupSelector> parseGroupingsWithInvocation(
            DataSeries invocationDataSeries,
            List<ColumnSelector> columnSelectors
    ) {
        return columnSelectors
                .stream()
                .filter(ColumnSelector::getIsGrouping)
                .map(columnSelector -> TimeSeriesGroupSelector.builder()
                            .setColumn(
                                    invocationDataSeries.getColumns()
                                            .stream()
                                            .filter(column ->
                                                    column.getName().equals(
                                                            columnSelector.getColumnName()
                                                    )
                                            )
                                            .findFirst()
                                            .orElseThrow(
                                                    () -> new NotFoundException(
                                                            "Column not found")
                                            )
                            )
                            .setAlias(columnSelector.getAlias())
                            .build()
                )
                .collect(Collectors.toList());
    }

    public TimeSeriesResultOutputSelector parseTimeSeriesResultOutputSelectors(
            SeriesResult invocationSeriesResult,
            List<OutputColumn> outputColumns
    ) {
        // Needs parsing if worker def output columns get aggregations
        return TimeSeriesResultOutputSelector.builder()
                .setSeriesResult(invocationSeriesResult)
                .setResultOutputColumnSelectors(
                        parseTimeSeriesResultOutputSelectors(
                                outputColumns
                        )
                )
                .setResultOutputGroupings(
                        Collections.emptyList()
                )
                .build();
    }

    private List<TimeSeriesResultOutputColumnSelector> parseTimeSeriesResultOutputSelectors(
            List<OutputColumn> outputColumns
    ) {
        return outputColumns
                .stream()
                .map(outputColumn -> TimeSeriesResultOutputColumnSelector
                        .builder()
                        .setOutputColumn(
                                OutputColumn.builder()
                                        .setId(-1L)
                                        .setIndex(outputColumn.getIndex())
                                        .setColumnName(outputColumn.getColumnName())
                                        .setType(outputColumn.getType())
                                        .build()
                        )
                        .setAlias(outputColumn.getAlias())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public Map<String, ?> mapGroupingParamsToValues(
            TimeSeriesSelector selection,
            List<?> values
    ) {
        if (selection.getSeriesResultOutput() != null) {
            return mapResultOutputGroupingParamsToValues(selection, values);
        }
        if (selection.getGroupings().size() != values.size()) {
            throw new InputException("Grouping parameters are missing values");
        }

        return IntStream.range(0, selection.getGroupings().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> {
                            TimeSeriesQuerySelector.Builder builder =
                                    TimeSeriesQuerySelector
                                            .builder()
                                            .setModifier(TimeSeriesModifier.group_by);
                            if (selection.getDataSeries() != null) {
                                builder = builder.setDataSeriesColumnSelector(
                                        DataSeriesColumnSelector.builder()
                                                .setName(selection
                                                        .getDataSeries()
                                                        .getName())
                                                .setColumnName(selection
                                                        .getGroupings()
                                                        .get(i)
                                                        .getColumn()
                                                        .getName()
                                                )
                                                .build()
                                );
                            }
                            else {
                                Invocation invocation = Objects
                                        .requireNonNull(
                                                Objects
                                                        .requireNonNull(
                                                                selection.getSeriesResult()
                                                        )
                                                        .getInvocation()
                                        );
                                builder = builder.setSeriesResultColumnSelector(
                                        SeriesResultColumnSelector.builder()
                                                .setName(invocation
                                                        .getTask()
                                                        .getName()
                                                )
                                                .setColumnName(selection
                                                        .getGroupings()
                                                        .get(i)
                                                        .getColumn()
                                                        .getName()
                                                )
                                                .setInvocationNumber(
                                                        invocation.getInvocationNumber()
                                                )
                                                .build()
                                );
                            }
                            return builder
                                    .build()
                                    .toString();
                        },
                        values::get
                ));
    }

    public Map<String, ?> mapWorkerInputGroupingParamsToValues(
            TimeSeriesSelector selection,
            List<?> values,
            List<ColumnSelector> columnSelectors
    ) {
        if (selection.getGroupings().size() != values.size()) {
            throw new InputException("Grouping parameters are missing values");
        }

        return IntStream.range(0, selection.getGroupings().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> columnSelectors
                                .stream()
                                .filter(columnSelector ->
                                        columnSelector.getColumnName()
                                                .equals(
                                                        selection.getGroupings()
                                                                .get(i)
                                                                .getColumn()
                                                                .getName()
                                                )
                                )
                                .findFirst()
                                .map(columnSelector ->
                                        columnSelector.getWorkerDefColumn().getName()
                                )
                                .orElse(
                                        selection.getGroupings()
                                                .get(i)
                                                .getColumn()
                                                .getName()
                                ),
                        values::get
                ));
    }

    public Map<String, ?> mapResultOutputGroupingParamsToValues(
            TimeSeriesSelector selection,
            List<?> values
    ) {
        if (selection.getResultOutputGroupings().size() != values.size()) {
            throw new InputException("Grouping parameters are missing values");
        }
        return IntStream.range(0, selection.getResultOutputGroupings().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> {
                            TimeSeriesQuerySelector.Builder builder =
                                    TimeSeriesQuerySelector
                                            .builder()
                                            .setModifier(TimeSeriesModifier.group_by);

                            Invocation invocation = Objects
                                    .requireNonNull(
                                            Objects
                                                    .requireNonNull(
                                                            selection.getSeriesResultOutput()
                                                    )
                                                    .getInvocation()
                                    );
                            builder = builder.setSeriesResultOutputColumnSelector(
                                    SeriesResultOutputColumnSelector.builder()
                                            .setName(invocation
                                                    .getTask()
                                                    .getName()
                                            )
                                            .setColumnName(selection
                                                    .getResultOutputGroupings()
                                                    .get(i)
                                                    .getOutputColumn()
                                                    .getColumnName()
                                            )
                                            .setInvocationNumber(
                                                    invocation.getInvocationNumber()
                                            )
                                            .build()
                            );

                            return builder
                                    .build()
                                    .toString();
                        },
                        values::get
                ));
    }

    public Map<String, ?> mapResultOutputGroupingParamsToValues(
            TimeSeriesResultOutputSelector selection,
            List<?> values
    ) {
        if (selection.getResultOutputGroupings().size() != values.size()) {
            throw new InputException("Grouping parameters are missing values");
        }
        return IntStream.range(0, selection.getResultOutputGroupings().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> {
                            TimeSeriesQuerySelector.Builder builder =
                                    TimeSeriesQuerySelector
                                            .builder()
                                            .setModifier(TimeSeriesModifier.group_by);
                            if (selection.getSeriesResult() != null) {
                                Invocation invocation = Objects
                                        .requireNonNull(
                                                Objects
                                                        .requireNonNull(
                                                                selection.getSeriesResult()
                                                        )
                                                        .getInvocation()
                                        );
                                builder = builder.setSeriesResultColumnSelector(
                                        SeriesResultColumnSelector.builder()
                                                .setName(invocation
                                                        .getTask()
                                                        .getName()
                                                )
                                                .setColumnName(selection
                                                        .getResultOutputGroupings()
                                                        .get(i)
                                                        .getOutputColumn()
                                                        .getColumnName()
                                                )
                                                .setInvocationNumber(
                                                        invocation.getInvocationNumber()
                                                )
                                                .build()
                                );
                            }
                            return builder
                                    .build()
                                    .toString();
                        },
                        values::get
                ));
    }

    public TimeSeriesQuery validateQuery(TimeSeriesQuery query) {
        if (query.getStart() == null) {
            query = query.withStart(Instant.parse("1970-01-01T00:00:00.00Z"));
        }
        if (query.getEnd() == null) {
            query = query.withEnd(Instant.parse("2100-12-31T23:59:59.00Z"));
        }
        if (query.getSelectors().size() == 0) {
            throw new InputException("No data selected, 'selectors' is required");
        }
        return query;
    }

    public void validateSelection(
            TimeSeriesQuery query,
            TimeSeriesSelector selection
    ) {
        if (selection.getGroupings().size() <= 0) {
            if (Objects.isNull(query.getInterval())) {
                // Has useless agg
                if (selection.getColumnSelectors().size() > 0) {
                    selection.getColumnSelectors()
                            .stream()
                            .filter(columnSelector -> columnSelector.getModifier() != null)
                            .findFirst()
                            .ifPresent(columnSelector -> {
                                throw new InputException(
                                        "Aggregation on column \"" + columnSelector.getColumn()
                                                + "\" is invalid without grouping or time-bucket."
                                );
                            });
                }
            }
            else {
                // Missing agg for time-bucket
                if (!selection.isRawData()) {
                    // Only check for agg if not selecting raw data
                    selection.getColumnSelectors()
                            .stream()
                            .filter(columnSelector -> columnSelector.getModifier() == null)
                            .findFirst()
                            .ifPresent(columnSelector -> {
                                throw new InputException(
                                        "Aggregation on column \"" + columnSelector.getColumn()
                                                + "\" is required when used with time-bucket."
                                );
                            });
                }
            }
        }
        else if (selection.getResultOutputGroupings().size() <= 0) {
            if (Objects.isNull(query.getInterval())) {
                if (selection.getResultOutputColumnSelectors().size() > 0) {
                    selection.getResultOutputColumnSelectors()
                            .stream()
                            .filter(resultOutputColumnSelector ->
                                    resultOutputColumnSelector.getModifier() != null
                            )
                            .findFirst()
                            .ifPresent(resultOutputColumnSelector -> {
                                throw new InputException(
                                        "Aggregation on column \""
                                                + resultOutputColumnSelector.getOutputColumn()
                                                + "\" is invalid without grouping or time-bucket."
                                );
                            });
                }
            }
            else {
                if (!selection.isRawWorkerData()) {
                    // Only check for agg if not selecting raw data
                    Objects.requireNonNull(selection.getResultOutputColumnSelectors())
                            .stream()
                            .filter(resultOutputColumnSelector ->
                                    resultOutputColumnSelector.getModifier() == null
                            )
                            .findFirst()
                            .ifPresent(resultOutputColumnSelector -> {
                                throw new InputException(
                                        "Aggregation on column \""
                                                + resultOutputColumnSelector.getOutputColumn()
                                                + "\" is required when used with time-bucket."
                                );
                            });
                }
            }
        }
        else {
            if (selection.getColumnSelectors().size() > 0) {
                selection.getColumnSelectors()
                        .stream()
                        .filter(columnSelector -> columnSelector.getModifier() == null)
                        .findFirst()
                        .ifPresent(columnSelector -> {
                            throw new InputException(
                                    "Aggregation on column \"" + columnSelector.getColumn()
                                            + "\" is required when used with grouping."
                            );
                        });
            }
            else if (selection.getResultOutputColumnSelectors().size() > 0) {
                selection.getResultOutputColumnSelectors()
                        .stream()
                        .filter(resultOutputColumnSelector ->
                                resultOutputColumnSelector.getModifier() == null
                        )
                        .findFirst()
                        .ifPresent(resultOutputColumnSelector -> {
                            throw new InputException(
                                    "Aggregation on column \""
                                            + resultOutputColumnSelector.getOutputColumn()
                                            + "\" is required when used with grouping."
                            );
                        });
            }

        }
    }

    public Stream<List<String>> convertFromMeasurement(
            Invocation invocation,
            List<Measurement> measurements
    ) {
        return measurements.stream()
                .map(mapper(invocation));
    }

    public Stream<List<String>> convertFromMeasurement(
            List<Column> columns,
            List<Measurement> measurements
    ) {
        return measurements.stream()
                .map(mapper(columns));
    }

    private Function<Measurement, List<String>> mapper(
            Invocation invocation
    ) {
        List<WorkerDefColumn> workerDefOutputColumns = invocation.getWorker()
                .getDefinition()
                .getColumns()
                .stream()
                .filter(workerDefColumn -> workerDefColumn.getColumnType()
                        .equals(WorkerDefColumnType.output)
                )
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        if (workerDefOutputColumns
                .stream()
                .findFirst()
                .isPresent()
                && workerDefOutputColumns
                .stream()
                .findFirst()
                .get()
                .getValueType()
                .getFormat() != null
        ) {
            formatter = DateTimeFormatter.ofPattern(Objects.requireNonNull(workerDefOutputColumns
                    .stream()
                    .findFirst()
                    .get()
                    .getValueType()
                    .getFormat())
            );
        }

        DateTimeFormatter finalFormatter = formatter.withZone(ZoneOffset.UTC);

        return measurement -> Stream
                .concat(
                        Stream.of(finalFormatter.format(measurement.getTime())),
                        measurement
                                .getValues()
                                .values()
                                .stream()
                                .map(Object::toString)
                )
                .collect(Collectors.toList());
    }

    private Function<Measurement, List<String>> mapper(
            List<Column> columns
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                Objects.requireNonNull(
                        columns
                                .get(0)
                                .getType()
                                .getFormat()
                )
        );
        return measurement -> Stream
                .concat(
                        Stream.of(formatter.format(measurement.getTime())),
                        measurement.getValues()
                                .values()
                                .stream()
                                .map(Object::toString)
                )
                .collect(Collectors.toList());
    }
}
