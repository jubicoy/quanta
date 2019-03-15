package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.ExternalClientController;
import fi.jubic.quanta.models.ExternalClient;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;

@Path("external-clients")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class ExternalClientResource {
    private final ExternalClientController externalClientController;

    @Inject
    public ExternalClientResource(
            ExternalClientController externalClientController
    ) {
        this.externalClientController = externalClientController;
    }

    @GET
    @Path("{task-id}")
    public List<ExternalClient> getExternalClientsOfTask(
            @PathParam("task-id") Long taskId
    ) {
        return externalClientController.getAllOfTask(taskId);
    }

    @POST
    @Path("generate/{task-id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ExternalClient generateExternalClient(
            @PathParam("task-id") Long taskId,
            ExternalClient externalClient
    ) {
        return externalClientController.generateExternalClient(taskId, externalClient);
    }

    @DELETE
    @Path("{id}/delete")
    public Response deleteExternalClient(
            @PathParam("id") Long externalClientId
    ) {
        ExternalClient deleted = externalClientController
                .deleteExternalClient(externalClientId);

        if (Objects.nonNull(deleted.getDeletedAt())) {
            return Response.ok().build();
        }
        else {
            return Response.serverError().build();
        }
    }
}
