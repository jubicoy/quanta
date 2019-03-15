package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.InvocationQuery;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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