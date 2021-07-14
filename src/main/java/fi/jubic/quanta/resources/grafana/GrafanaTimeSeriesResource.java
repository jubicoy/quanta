package fi.jubic.quanta.resources.grafana;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.controller.TimeSeriesController;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesQuery;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.TimeSeriesQuery;
import fi.jubic.quanta.models.request.grafana.GrafanaQueryRequest;
import fi.jubic.quanta.models.response.grafana.GrafanaQueryResponse;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/grafana")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GrafanaTimeSeriesResource {
    private final DataController dataController;
    private final TimeSeriesController timeSeriesController;

    @Inject
    public GrafanaTimeSeriesResource(
            DataController dataController,
            TimeSeriesController timeSeriesController
    ) {
        this.dataController = dataController;
        this.timeSeriesController = timeSeriesController;
    }

    @Path("/")
    @GET
    public Response test() {
        return Response.ok().build();
    }

    @Path("/search")
    @POST
    public List<DataSeries> search(
            @BeanParam DataSeriesQuery query
    ) {
        return dataController.searchSeries(query);
    }

    @Path("/query")
    @POST
    public List<GrafanaQueryResponse> query(
            GrafanaQueryRequest grafanaQueryRequest,
            @BeanParam Pagination pagination
    ) {
        Map<String, String> dataSource1 = grafanaQueryRequest.getTargets().get(0);
        String target = dataSource1.get("target");

        DataSeries series = dataController.getSeriesDetailsByName(target)
                .orElseThrow(NotFoundException::new);

        List<String> selectorsString = series.getColumns()
                .stream()
                .filter(column -> !Objects.requireNonNull(column.getName())
                        .equals("time")
                )
                .map(column -> String.format(
                        "series:%s.%s",
                        series.getName(),
                        column.getName()
                ))
                .collect(Collectors.toList());

        GrafanaQueryResponse response = GrafanaQueryResponse.builder()
                .setTarget(target)
                .setDatapoints(
                        timeSeriesController
                                .query(new TimeSeriesQuery()
                                        .withFilters(selectorsString)
                                        .withStart(grafanaQueryRequest.getRange().getFrom())
                                        .withEnd(grafanaQueryRequest.getRange().getTo())
                                        .withIntervalSeconds(
                                                grafanaQueryRequest.getIntervalMs() / 1000
                                        ),
                                        pagination
                                )
                                .stream()
                                .findFirst()
                                .map(QueryResult::getMeasurements)
                                .orElseThrow(BadRequestException::new)
                                .stream()
                                .map(measurement -> Stream
                                        .concat(
                                                measurement.getValues().values().stream(),
                                                Stream.of(measurement.getTime())
                                        )
                                        .collect(Collectors.toList())
                                )
                                .collect(Collectors.toList())
                )
                .build();
        return Collections.singletonList(response);
    }
}
