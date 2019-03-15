package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
