package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionQuery;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesQuery;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
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
    @Path("data-series")
    public List<DataSeries> getAll(
            @BeanParam DataSeriesQuery query
    ) {
        return dataController.searchDataSeries(query);
    }

    @GET
    @Path("{id}")
    public DataConnection getDetails(
            @PathParam("id") Long id
    ) {
        return dataController.getConnectionDetails(id)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("data-series/{id}")
    public DataSeries getDataSeriesDetails(
            @PathParam("id") Long id
    ) {
        return dataController.getDataSeriesDetails(id)
                .orElseThrow(NotFoundException::new);
    }

    @PUT
    public DataConnection updateDataConnection(DataConnection dataConnection) {
        return dataController.updateDataConnection(dataConnection);
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
    @Path("{id}/result")
    public List<List<String>> getResult(
            @PathParam("id") Long dataConnectionId,
            DataSeries dataSeries
    ) {
        return dataController.getResult(dataSeries);
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
    @Path("data-series/{id}")
    public DataSeries deleteDataSeries(
            @PathParam("id") Long dataSeriesId
    ) {
        return dataController.deleteSeries(
                dataSeriesId
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
