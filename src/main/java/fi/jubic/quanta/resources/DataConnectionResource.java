package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionQuery;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Singleton
@Path("data-connections")
@RolesAllowed({"ADMIN", "USER"})
@Produces(MediaType.APPLICATION_JSON)
public class DataConnectionResource {
    private final DataController dataController;

    @Inject
    DataConnectionResource(
            DataController dataController
    ) {
        this.dataController = dataController;
    }

    @GET
    public List<DataConnection> getAll(
            @BeanParam DataConnectionQuery query
    ) {

        return dataController.searchConnections(query);
    }

    @GET
    @Path("{id}")
    public DataConnection getDetails(
            @PathParam("id") Long id
    ) {
        return dataController.getConnectionDetailsWithEmptyLogin(id)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("{id}/metadata")
    public DataConnectionMetadata getMetadata(
            @PathParam("id") Long id
    ) {
        return dataController.getConnectionMetadata(id)
                .orElseThrow(NotFoundException::new);
    }

    @POST
    public DataConnection create(DataConnection dataConnection) {
        return dataController.create(dataConnection);
    }

    @POST
    @Path("{id}/sample")
    public DataSample getSample(
            @PathParam("id") Long dataConnectionId,
            DataSeries dataSeries
    ) {
        return dataController.getSample(dataConnectionId, dataSeries);
    }

    @POST
    @Path("test")
    public Response test(
            DataConnection dataConnection
    ) {
        if (!dataController.test(dataConnection)) {
            throw new NotAuthorizedException("Not Authorized");
        }
        return Response.ok().build();
    }

    @POST
    @Path("{id}/data-series/{skipImportData}")
    public DataSeries createDataSeries(
            @PathParam("id") Long dataConnectionId,
            @PathParam("skipImportData") Boolean skipImportData,
            DataSeries dataSeries
    ) {
        return dataController.create(
                dataConnectionId,
                dataSeries,
                skipImportData
        );
    }

    @DELETE
    @Path("{id}")
    public DataConnection delete(
            @PathParam("id") Long dataConnectionId
    ) {
        return dataController.delete(
                dataConnectionId
        );
    }
}
