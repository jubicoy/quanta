package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Task;
import fi.jubic.quanta.models.TaskQuery;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("tasks")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class TaskResource {
    private final TaskController taskController;

    @Inject
    public TaskResource(
            TaskController taskController
    ) {
        this.taskController = taskController;
    }

    @GET
    public List<Task> getTasks(
            @BeanParam TaskQuery query
    ) {
        return taskController.search(query);
    }

    @GET
    @Path("{id}")
    public Task getTask(
            @PathParam("id") Long id
    ) {
        return taskController.getDetails(id)
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Task submitTask(Task task) {
        return taskController.create(task);
    }

    @PUT
    public Task updateTask(Task task) {
        return taskController.update(task);
    }

    @POST
    @Path("{id}/invoke")
    @Consumes(MediaType.APPLICATION_JSON)
    public Invocation invokeTask(
            @PathParam("id") Long id
    ) {
        return taskController.invoke(id);
    }

    @DELETE
    @Path("{id}")
    public Task delete(
            @PathParam("id") Long id
    ) {
        return taskController.delete(id);
    }
}
