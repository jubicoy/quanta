package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TimeSeriesController;
import fi.jubic.quanta.external.exporter.CsvExporter;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.TimeSeriesQuery;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("query")
@RolesAllowed({"ADMIN", "USER"})
public class TimeSeriesResource {
    private final TimeSeriesController timeSeriesController;
    private final CsvExporter csvExporter;

    @Inject
    TimeSeriesResource(
            TimeSeriesController timeSeriesController,
            CsvExporter csvExporter
    ) {
        this.timeSeriesController = timeSeriesController;
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
}
