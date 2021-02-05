package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.ExternalClientController;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.ExternalClientQuery;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.models.view.ExternalClientView;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Path("external-clients")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class ExternalClientResource {
    private final ExternalClientController externalClientController;
    private final QuantaAuthenticator quantaAuthenticator;

    @Inject
    public ExternalClientResource(
            ExternalClientController externalClientController,
            QuantaAuthenticator quantaAuthenticator
    ) {
        this.externalClientController = externalClientController;
        this.quantaAuthenticator = quantaAuthenticator;
    }

    @GET
    public List<ExternalClientView> getExternalClients(
            @Context User user
    ) {
        return externalClientController
                .search(
                        new ExternalClientQuery(user.getId())
                )
                .stream()
                .map(ExternalClientView::of)
                .collect(Collectors.toList());
    }

    @GET
    @Path("{task-id}")
    public List<ExternalClient> getExternalClientsOfTask(
            @PathParam("task-id") Long taskId
    ) {
        return externalClientController.getAllOfTask(taskId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ExternalClientView generateExternalClient(
            @Context User user,
            ExternalClientView externalClientView
    ) {
        ExternalClient externalClient;
        if (externalClientView.getTask() != null) {
            externalClient = externalClientController
                     .generateExternalClient(
                             externalClientView.getTask().getId(),
                             externalClientView.getName(),
                             externalClientView.getDescription(),
                             user
                     );
        }
        else {
            externalClient = externalClientController
                    .generateExternalClient(
                            externalClientView.getName(),
                            externalClientView.getDescription(),
                            user
                    );
        }

        quantaAuthenticator.reloadExternalClients();
        return ExternalClientView.of(externalClient);
    }

    @DELETE
    @Path("{id}/delete")
    public Response deleteExternalClient(
            @Context User user,
            @PathParam("id") Long externalClientId
    ) {
        ExternalClient deleted = externalClientController
                .deleteExternalClient(externalClientId);

        if (Objects.nonNull(deleted.getDeletedAt())) {
            quantaAuthenticator.reloadExternalClients();
            return Response.ok().build();
        }
        else {
            return Response.serverError().build();
        }
    }
}
