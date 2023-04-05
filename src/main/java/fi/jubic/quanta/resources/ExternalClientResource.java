package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.ExternalClientController;
import fi.jubic.quanta.models.ExternalClient;
import fi.jubic.quanta.models.ExternalClientQuery;
import fi.jubic.quanta.models.QuantaAuthenticator;
import fi.jubic.quanta.models.User;
import fi.jubic.quanta.models.view.ExternalClientView;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
