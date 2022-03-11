package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.DataSeriesDao;
import fi.jubic.quanta.dao.ExternalClientDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.SeriesResultDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.dao.TimeSeriesDao;
import fi.jubic.quanta.domain.ExternalClientDomain;
import fi.jubic.quanta.domain.TimeSeriesDomain;
import fi.jubic.quanta.exception.AuthorizationException;
import fi.jubic.quanta.exception.InputException;
import fi.jubic.quanta.models.ColumnSelector;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationStatus;
import fi.jubic.quanta.models.Measurement;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.SeriesResult;
import fi.jubic.quanta.models.SeriesResultQuery;
import fi.jubic.quanta.models.SeriesResultSelector;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TimeSeriesFilter;
import fi.jubic.quanta.models.TimeSeriesQuery;
import fi.jubic.quanta.models.TimeSeriesQuerySelector;
import fi.jubic.quanta.models.TimeSeriesResultOutputFilter;
import fi.jubic.quanta.models.TimeSeriesResultOutputSelector;
import fi.jubic.quanta.models.TimeSeriesSelector;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class TimeSeriesController {
    private final TimeSeriesDomain timeSeriesDomain;
    private final ExternalClientDomain externalClientDomain;
    private final DataSeriesDao dataSeriesDao;
    private final SeriesResultDao seriesResultDao;
    private final TimeSeriesDao timeSeriesDao;
    private final TaskDao taskDao;
    private final InvocationDao invocationDao;
    private final ExternalClientDao externalClientDao;

    @Inject
    TimeSeriesController(
            TimeSeriesDomain timeSeriesDomain,
            ExternalClientDomain externalClientDomain,
            TimeSeriesDao timeSeriesDao,
            SeriesResultDao seriesResultDao,
            DataSeriesDao dataSeriesDao,
            TaskDao taskDao,
            InvocationDao invocationDao,
            ExternalClientDao externalClientDao
    ) {
        this.timeSeriesDomain = timeSeriesDomain;
        this.externalClientDomain = externalClientDomain;
        this.dataSeriesDao = dataSeriesDao;
        this.seriesResultDao = seriesResultDao;
        this.timeSeriesDao = timeSeriesDao;
        this.taskDao = taskDao;
        this.invocationDao = invocationDao;
        this.externalClientDao = externalClientDao;
    }

    public List<Measurement> loadRawInvocationData(
            Invocation invocation,
            Pagination pagination
    ) {
        DataSeries invocationDataSeries = invocation.getColumnSelectors()
                .stream()
                .findFirst()
                .map(ColumnSelector::getSeries)
                .orElseThrow(IllegalStateException::new);
        DataSeries dataSeries = dataSeriesDao.getDetails(invocationDataSeries.getId())
                .orElseThrow(IllegalStateException::new);

        return timeSeriesDao.select(dataSeries, pagination)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> loadInvocationDataFromDataSeries(
            Invocation invocation,
            String seriesKey,
            Pagination pagination
    ) {
        List<ColumnSelector> columnSelectors = new ArrayList<>(Collections.emptyList());
        if (Objects.nonNull(seriesKey)) {
            columnSelectors.addAll(
                    invocation.getColumnSelectors()
                            .stream()
                            .filter(
                                    columnSelector -> Objects.nonNull(
                                            columnSelector.getWorkerDefColumn()
                                    )
                            )
                            .filter(
                                    columnSelector -> columnSelector
                                            .getWorkerDefColumn()
                                            .getSeriesKey()
                                            .equals(seriesKey)
                            )
                            .collect(Collectors.toList())
            );
        }
        else {
            columnSelectors.addAll(invocation.getColumnSelectors());
        }

        DataSeries invocationDataSeries = getInvocationDataSeries(columnSelectors);

        List<DataSeries> listOfInvocationDataSeries = new ArrayList<>();
        listOfInvocationDataSeries.add(invocationDataSeries);

        List<TimeSeriesSelector> selections = listOfInvocationDataSeries
                .stream()
                .map(dataSeries ->
                timeSeriesDomain.parseTimeSeriesSelectorsWithInvocation(
                        dataSeries,
                        columnSelectors
                )
        ).collect(Collectors.toList());

        return getWorkerInputs(selections, columnSelectors, pagination);
    }

    public List<Measurement> loadInvocationDataFromSeriesResult(
            Invocation invocation,
            Pagination pagination
    ) {
        if (!invocation.getStatus().equals(InvocationStatus.Completed)) {
            return Collections.emptyList();
        }
        List<SeriesResult> completedInvocationSeriesResults = seriesResultDao
                .search(
                        new SeriesResultQuery()
                                .withInvocationId(
                                        invocation.getId()
                                )
                );

        SeriesResult invocationSeriesResult = seriesResultDao.getDetails(
                completedInvocationSeriesResults.stream()
                        .reduce((a, b) -> b) // Get last series result
                        .get()
                        .getId()
        ).orElseThrow(NotFoundException::new);

        List<SeriesResult> listOfInvocationSeriesResults = new ArrayList<>();
        listOfInvocationSeriesResults.add(invocationSeriesResult);

        List<TimeSeriesResultOutputSelector> selections = listOfInvocationSeriesResults
                .stream()
                .map(seriesResult ->
                        timeSeriesDomain.parseTimeSeriesResultOutputSelectors(
                                seriesResult,
                                invocation.getOutputColumns()
                        )
                ).collect(Collectors.toList());

        return getWorkerOutputs(selections, invocation, pagination);
    }

    public List<QueryResult> externalQuery(
            TimeSeriesQuery query,
            String externalClientToken,
            Pagination pagination
    ) {
        TimeSeriesQuery validatedQuery = timeSeriesDomain.validateQuery(query);
        ExternalClient externalClient = externalClientDomain.validate(
                externalClientDao
                        .getDetails(externalClientToken)
                        .orElseThrow(
                                () -> new AuthorizationException("Invalid client")
                        )
        );

        List<TimeSeriesQuerySelector> selectors = timeSeriesDomain.getSelectorsLimitedToTask(
                timeSeriesDomain.parseSelectors(validatedQuery),
                externalClient.getTask()
        );
        if (selectors.isEmpty()) {
            throw new NotFoundException();
        }
        return query(validatedQuery, selectors, pagination);
    }

    public List<QueryResult> query(
            TimeSeriesQuery query,
            Pagination pagination
    ) {
        TimeSeriesQuery validatedQuery = timeSeriesDomain.validateQuery(query);
        List<TimeSeriesQuerySelector> selectors = timeSeriesDomain.parseSelectors(validatedQuery);
        return query(validatedQuery, selectors, pagination);
    }

    public List<QueryResult> query(
            TimeSeriesQuery validatedQuery,
            List<TimeSeriesQuerySelector> selectors,
            Pagination pagination
    ) {
        Map<String, List<TimeSeriesQuerySelector>> dataSeriesSelections =
                timeSeriesDomain.parseDataSeriesSelections(selectors);

        Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> seriesResultSelections =
                mapLatestToInvocationNumber(
                        timeSeriesDomain.parseSeriesResultsSelections(selectors)
                );

        Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> seriesResultOutputSelections =
                mapLatestToInvocationNumber(
                        timeSeriesDomain.parseSeriesResultOutputSelections(selectors)
                );

        Map<String, DataSeries> dataSeries = dataSeriesSelections
                .keySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                name -> dataSeriesDao
                                        .getDetailsByName(name)
                                        .orElseThrow(NotFoundException::new)
                        )
                );

        Map<SeriesResultSelector, SeriesResult> seriesResults =
                mapSelectorToSeriesResult(seriesResultSelections);

        Map<SeriesResultSelector, SeriesResult> seriesResultOutputs =
                mapSelectorToSeriesResult(seriesResultOutputSelections);

        List<TimeSeriesSelector> selections = Stream
                .of(
                        dataSeries
                                .entrySet()
                                .stream()
                                .map(
                                        entry -> timeSeriesDomain.parseTimeSeriesSelectors(
                                                entry.getValue(),
                                                dataSeriesSelections.get(entry.getKey())
                                        )
                                ),
                        seriesResults
                                .entrySet()
                                .stream()
                                .map(
                                        entry -> timeSeriesDomain.parseTimeSeriesSelectors(
                                                entry.getValue(),
                                                getSeriesResultInvocationDataSeries(
                                                        entry.getValue()
                                                ),
                                                seriesResultSelections.get(entry.getKey())
                                        )
                                ),
                        seriesResultOutputs
                                .entrySet()
                                .stream()
                                .map(
                                        entry -> timeSeriesDomain.parseTimeSeriesSelectors(
                                                entry.getValue(),
                                                getSeriesResultInvocation(
                                                        entry.getValue()
                                                ),
                                                seriesResultOutputSelections.get(entry.getKey())
                                        )

                                )
                )
                .flatMap(Function.identity())
                .collect(Collectors.toList());

        return selections
                .stream()
                .peek(
                        selection -> timeSeriesDomain.validateSelection(
                                validatedQuery,
                                selection
                        )
                )
                .flatMap(selection ->
                        timeSeriesDao.query(
                                validatedQuery,
                                selection,
                                getDataSeriesFromSelector(selection),
                                pagination
                        )
                                .entrySet()
                                .stream()
                                .map(entry -> QueryResult.builder()
                                        .setDataSeriesId(
                                                Objects.nonNull(selection.getDataSeries())
                                                        ? selection.getDataSeries().getId()
                                                        : null
                                        )
                                        .setSeriesResultId(
                                                getSeriesResultIdFromSelection(selection)
                                        )
                                        .setQueryFilters(
                                            Stream.of(
                                                    selection
                                                            .getFilters()
                                                            .stream()
                                                            .map(
                                                                    TimeSeriesFilter
                                                                            ::getFullFilterString
                                                            ),
                                                    selection
                                                            .getResultOutputFilters()
                                                            .stream()
                                                            .map(
                                                                    TimeSeriesResultOutputFilter
                                                                            ::getFullFilterString
                                                            )
                                            )
                                                .flatMap(Function.identity())
                                                .collect(Collectors.toList())
                                        )
                                        .setMeasurements(
                                                addGroupingParamsToMeasurements(
                                                        entry.getValue(),
                                                        timeSeriesDomain
                                                                .mapGroupingParamsToValues(
                                                                        selection,
                                                                        entry.getKey()
                                                                )
                                                )
                                        )
                                        .build())
                )
                .collect(Collectors.toList());
    }

    private Map<SeriesResultSelector, SeriesResult> mapSelectorToSeriesResult(
            Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> seriesResultSelections
    ) {
        return seriesResultSelections
                .keySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                seriesResultSelector -> {
                                    Task task = taskDao
                                            .getDetails(seriesResultSelector.getTaskName())
                                            .orElseThrow(NotFoundException::new);
                                    Invocation invocation = invocationDao
                                            .getDetails(
                                                    task.getId(),
                                                    seriesResultSelector.getInvocationNumber()
                                            )
                                            .orElseThrow(NotFoundException::new);
                                    if (invocation.getStatus() != InvocationStatus.Completed) {
                                        throw new InputException(
                                                "Incomplete Invocation can not be queried."
                                        );
                                    }
                                    List<SeriesResult> tempList = seriesResultDao
                                            .search(
                                                    new SeriesResultQuery()
                                                            .withInvocationId(
                                                                    invocation.getId()
                                                            )
                                            );
                                    if (tempList.size() <= 0) {
                                        throw new NotFoundException(
                                                "Found no SeriesResult of this Invocation."
                                        );
                                    }
                                    return seriesResultDao
                                            .getDetails(tempList.get(0).getId())
                                            .orElseThrow(IllegalStateException::new);
                                }
                        )
                );
    }

    private Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> mapLatestToInvocationNumber(
            Map<SeriesResultSelector, List<TimeSeriesQuerySelector>> seriesResultSelectorListMap
    ) {
        return seriesResultSelectorListMap
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                entry -> {
                                    SeriesResultSelector selector = entry.getKey();
                                    if (selector.getIsLatest()) {
                                        Task task = taskDao
                                                .getDetails(
                                                        selector.getTaskName()
                                                )
                                                .orElseThrow(NotFoundException::new);
                                        return selector
                                                .toBuilder()
                                                .setIsLatest(false)
                                                .setInvocationNumber(invocationDao
                                                        .getLatestCompleteInvocationNumber(
                                                                task.getId()
                                                        )
                                                )
                                                .build();
                                    }
                                    return selector;
                                },
                                Map.Entry::getValue,
                                // Select first entry if duplicated
                                // Happens when latest Invocation is selected
                                // by both 'latest' and invocationNumber
                                (entry1, entry2) -> entry1
                        )
                );
    }

    private DataSeries getSeriesResultInvocationDataSeries(
            SeriesResult seriesResult
    ) {
        Invocation invocation = invocationDao
                .getDetails(
                        Objects
                                .requireNonNull(
                                        seriesResult.getInvocation()
                                )
                                .getId()
                )
                .orElseThrow(IllegalStateException::new);

        DataSeries invocationDataSeries = invocation.getColumnSelectors()
                .stream()
                .findFirst()
                .map(ColumnSelector::getSeries)
                .orElseThrow(IllegalStateException::new);

        return dataSeriesDao.getDetails(invocationDataSeries.getId())
                .orElseThrow(IllegalStateException::new);
    }

    private Invocation getSeriesResultInvocation(
            SeriesResult seriesResult
    ) {
        return invocationDao
                .getDetails(
                        Objects
                                .requireNonNull(
                                        seriesResult.getInvocation()
                                )
                                .getId()
                )
                .orElseThrow(IllegalStateException::new);
    }

    private DataSeries getInvocationDataSeries(
            List<ColumnSelector> columnSelectors
    ) {
        DataSeries invocationDataSeries = columnSelectors
                .stream()
                .findFirst()
                .map(ColumnSelector::getSeries)
                .orElseThrow(IllegalStateException::new);

        return dataSeriesDao.getDetails(invocationDataSeries.getId())
                .orElseThrow(IllegalStateException::new);
    }

    private List<Map<String, Object>> getWorkerInputs(
            List<TimeSeriesSelector> selections,
            List<ColumnSelector> columnSelectors,
            Pagination pagination
    ) {
        TimeSeriesQuery timeSeriesQuery = new TimeSeriesQuery()
                .withStart(Instant.EPOCH)
                .withEnd(Instant.now())
                .withInterval(
                        String.format(
                                "%ss",
                                60 * 60 * 24 * 7L
                        )
                )
                .withFilters(Collections.emptyList());
        TimeSeriesQuery[] finalTimeSeriesQuery = {timeSeriesQuery};

        return selections
                .stream()
                .flatMap(selection ->
                        timeSeriesDao.queryToWorkerInputFormat(
                                finalTimeSeriesQuery[0],
                                selection,
                                columnSelectors,
                                false,
                                pagination
                        )
                                .entrySet()
                                .stream()
                                .flatMap(
                                        entry -> addGroupingParamsToValues(
                                                entry.getValue(),
                                                timeSeriesDomain
                                                        .mapWorkerInputGroupingParamsToValues(
                                                                selection,
                                                                entry.getKey(),
                                                                columnSelectors
                                                        )
                                        )
                                        .stream()
                                )
                )
                .collect(Collectors.toList());
    }

    private List<Measurement> getWorkerOutputs(
            List<TimeSeriesResultOutputSelector> selections,
            Invocation invocation,
            Pagination pagination
    ) {
        TimeSeriesQuery timeSeriesQuery = new TimeSeriesQuery()
                .withStart(Instant.EPOCH)
                .withEnd(Instant.now())
                .withInterval(
                        String.format(
                                "%ss",
                                60 * 60 * 24 * 7L
                        )
                )
                .withFilters(Collections.emptyList());
        TimeSeriesQuery[] finalTimeSeriesQuery = {timeSeriesQuery};

        return selections
                .stream()
                .flatMap(selection ->
                        timeSeriesDao.queryWorkerOutputs(
                                finalTimeSeriesQuery[0],
                                selection,
                                invocation.getOutputColumns(),
                                false,
                                pagination
                        )
                                .entrySet()
                                .stream()
                                .flatMap(
                                        entry -> addGroupingParamsToMeasurements(
                                                entry.getValue(),
                                                timeSeriesDomain
                                                        .mapResultOutputGroupingParamsToValues(
                                                                selection,
                                                            entry.getKey()
                                                        )
                                        ).stream()
                                )
                )
                .collect(Collectors.toList());
    }

    private List<Measurement> addGroupingParamsToMeasurements(
            List<Measurement> originalMeasurements,
            Map<String, ?> groupingParams
    ) {
        return originalMeasurements
                .stream()
                .map(measurement -> measurement
                        .toBuilder()
                        .setValues(
                                addGroupingParamsToValues(
                                        measurement.getValues(),
                                        groupingParams
                                )
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> addGroupingParamsToValues(
            List<Map<String, Object>> originalValues,
            Map<String, ?> groupingParams
    ) {
        return originalValues
                .stream()
                .map(value ->
                        addGroupingParamsToValues(
                                value,
                                groupingParams
                        )

                )
                .collect(Collectors.toList());
    }

    private Map<String, Object> addGroupingParamsToValues(
            Map<String, Object> values,
            Map<String, ?> groupingParams
    ) {
        Map<String, Object> initializedValues = new HashMap<>(values);
        Map<String, Object> temp = new HashMap<>(groupingParams);
        temp.keySet().removeAll(values.keySet());
        if (temp.size() > 0) {
            initializedValues.putAll(temp);
        }
        return initializedValues;
    }

    private Long getSeriesResultIdFromSelection(
            TimeSeriesSelector selector
    ) {
        if (Objects.nonNull(selector.getSeriesResult())) {
            return selector.getSeriesResult().getId();
        }
        if (Objects.nonNull(selector.getSeriesResultOutput())) {
            return selector.getSeriesResultOutput().getId();
        }
        return null;
    }

    private DataSeries getDataSeriesFromSelector(
            TimeSeriesSelector selector
    ) {
        if (Objects.nonNull(selector.getDataSeries())) {
            return selector.getDataSeries();
        }
        if (Objects.nonNull(selector.getSeriesResult())) {
            return getSeriesResultInvocationDataSeries(
                    selector.getSeriesResult()
            );
        }
        return getSeriesResultInvocationDataSeries(
                selector.getSeriesResultOutput()
        );
    }
}
