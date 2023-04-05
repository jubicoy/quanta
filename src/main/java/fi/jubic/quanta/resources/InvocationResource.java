package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Path("invocations")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class InvocationResource {
    private final TaskController taskController;

    @Inject
    public InvocationResource(
            TaskController taskController
    ) {
        this.taskController = taskController;
    }

    @GET
    public List<Invocation> search(@BeanParam InvocationQuery query) {
        return taskController.searchInvocations(query);
    }

    @GET
    @Path("{id}")
    public Invocation getDetails(@PathParam("id") Long id) {
        return taskController.getInvocationDetails(id)
                .orElseThrow(NotFoundException::new);
    }
}
