package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Path("data-connection-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class MetadataResource {
    private final DataController dataController;

    @Inject
    public MetadataResource(
            DataController dataController
    ) {
        this.dataController = dataController;
    }

    @GET
    @Path("{type}")
    public TypeMetadata getTypeMetadata(
            @PathParam("type") String type
    ) {
        return dataController.getMetadata(type);
    }
}
