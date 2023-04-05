package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.AnomalyController;
import fi.jubic.quanta.controller.TimeSeriesController;
import fi.jubic.quanta.external.exporter.CsvExporter;
import fi.jubic.quanta.models.AnomalyResult;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.TimeSeriesQuery;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Path("query")
@RolesAllowed({"ADMIN", "USER"})
public class TimeSeriesResource {
    private final TimeSeriesController timeSeriesController;
    private final CsvExporter csvExporter;
    private final AnomalyController anomalyController;

    @Inject
    TimeSeriesResource(
            TimeSeriesController timeSeriesController,
            AnomalyController anomalyController,
            CsvExporter csvExporter
    ) {
        this.timeSeriesController = timeSeriesController;
        this.anomalyController = anomalyController;
        this.csvExporter = csvExporter;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueryResult> queryJson(
            @BeanParam TimeSeriesQuery query,
            @BeanParam Pagination pagination
    ) {
        return timeSeriesController.query(query, pagination);
    }

    @GET
    @Produces("text/csv")
    public Response queryCsv(
            @BeanParam TimeSeriesQuery query,
            @BeanParam Pagination pagination
    ) {
        return Response
                .ok(
                        csvExporter.timeSeriesExport(
                                timeSeriesController.query(query, pagination)
                        )
                )
                .build();
    }

    @GET
    @Path("anomaly")
    @Produces(MediaType.APPLICATION_JSON)
    public AnomalyResult queryAnomaly(
            @BeanParam TimeSeriesQuery query,
            @BeanParam Pagination pagination
    ) {
        return anomalyController.query(query, pagination);
    }
}
