package fi.jubic.quanta.resources.external;

import fi.jubic.quanta.controller.TimeSeriesController;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.TimeSeriesQuery;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Path("external/query")
@PermitAll
public class TimeSeriesResource {
    private final TimeSeriesController timeSeriesController;

    @Inject
    TimeSeriesResource(
            TimeSeriesController timeSeriesController
    ) {
        this.timeSeriesController = timeSeriesController;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueryResult> queryJson(
            @HeaderParam("Client-Token") String clientToken,
            @BeanParam TimeSeriesQuery query,
            @BeanParam Pagination pagination
    ) {
        return timeSeriesController.externalQuery(query, clientToken, pagination);
    }
}
