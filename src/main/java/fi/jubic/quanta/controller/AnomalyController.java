package fi.jubic.quanta.controller;

import fi.jubic.quanta.dao.AnomalyDao;
import fi.jubic.quanta.dao.InvocationDao;
import fi.jubic.quanta.dao.TaskDao;
import fi.jubic.quanta.domain.TimeSeriesDomain;
import fi.jubic.quanta.models.AnomalyQuerySelector;
import fi.jubic.quanta.models.AnomalyResult;
import fi.jubic.quanta.models.AnomalySelector;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TimeSeriesQuery;
import jakarta.ws.rs.NotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class AnomalyController {
    private final TaskDao taskDao;
    private final InvocationDao invocationDao;
    private final AnomalyDao anomalyDao;
    private final TimeSeriesDomain timeSeriesDomain;

    @Inject
    public AnomalyController(
            TaskDao taskDao,
            InvocationDao invocationDao,
            AnomalyDao anomalyDao,
            TimeSeriesDomain timeSeriesDomain
    ) {
        this.taskDao = taskDao;
        this.invocationDao = invocationDao;
        this.anomalyDao = anomalyDao;
        this.timeSeriesDomain = timeSeriesDomain;
    }

    public AnomalyResult query(
            TimeSeriesQuery timeSeriesQuery,
            Pagination pagination
    ) {

        TimeSeriesQuery validatedQuery = timeSeriesDomain.validateQuery(timeSeriesQuery);

        List<AnomalyQuerySelector> anomalyQueries = validatedQuery
                .parseAnomalySelector();

        AnomalySelector anomalySelector = AnomalySelector.parse(anomalyQueries);

        Task task = taskDao
                .getDetails(
                        Objects.requireNonNull(
                                anomalySelector.getAnomalyResultSelector()
                        ).getTaskName()
                )
                .orElseThrow(NotFoundException::new);

        Long invocationNumber = invocationDao
                .getLatestCompleteInvocationNumber(task.getId());
        Invocation invocation = invocationDao
                .getDetails(
                        task.getId(),
                        invocationNumber
                )
                .orElseThrow(NotFoundException::new);

        return anomalyDao.query(
                validatedQuery,
                invocation.getId(),
                anomalySelector,
                pagination
        );
    }
}
