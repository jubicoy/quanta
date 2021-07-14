package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TimeSeriesController;
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
import java.util.List;

@Singleton
@Path("query")
@RolesAllowed({"ADMIN", "USER"})
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
            @BeanParam TimeSeriesQuery query,
            @BeanParam Pagination pagination
    ) {
        return timeSeriesController.query(query, pagination);
    }
}
