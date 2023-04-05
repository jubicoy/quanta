package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.WorkerController;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.WorkerQuery;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import java.util.List;

@Path("workers")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class WorkerManageResource {
    private final WorkerController workerController;

    @Inject
    public WorkerManageResource(
            WorkerController workerController
    ) {
        this.workerController = workerController;
    }

    @GET
    public List<Worker> search(@BeanParam WorkerQuery query) {
        return workerController.search(query);
    }

    @GET
    @Path("{id}")
    public Worker getDetails(@PathParam("id") Long id) {
        return workerController.getDetails(id)
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("{id}/authorize")
    public Worker authorizeWorker(@PathParam("id") Long id) {
        return workerController.authorize(id);
    }

    @POST
    @Path("{id}/unauthorize")
    public Worker unauthorizeWorker(@PathParam("id") Long id) {
        return workerController.unauthorize(id);
    }

    @DELETE
    @Path("{id}")
    public Worker deleteWorker(@PathParam("id") Long id) {
        return workerController.delete(id);
    }
}
