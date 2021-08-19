package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TagController;
import fi.jubic.quanta.models.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Singleton
@Path("tags")
@RolesAllowed({"ADMIN", "USER"})
@Produces(MediaType.APPLICATION_JSON)
public class TagResource {
    private final TagController tagController;

    @Inject
    TagResource(
            TagController tagController
    ) {
        this.tagController = tagController;
    }

    @GET
    public List<Tag> getAll() {
        return tagController.getAll();
    }

    @GET
    @Path("tasks")
    public List<Long> searchTasks(
            @QueryParam("ids") List<Long> tagIds
    ) {
        return tagController.searchTasks(tagIds);
    }

    @GET
    @Path("data-connections")
    public List<Long> searchDataConnections(
            @QueryParam("ids") List<Long> tagIds
    ) {
        return tagController.searchDataConnections(tagIds);
    }

    @GET
    @Path("data-connections/{id}")
    public List<Tag> getDataConnectionTags(
            @PathParam("id") Long dataConnectionId
    ) {
        return tagController.getDataConnectionTags(
                dataConnectionId
        );
    }

    @GET
    @Path("tasks/{id}")
    public List<Tag> getTaskTags(
            @PathParam("id") Long taskId
    ) {
        return tagController.getTaskTags(
                taskId
        );
    }

    @POST
    @Path("data-connections/{id}")
    public List<Tag> updateDataConnectionTags(
            @PathParam("id") Long dataConnectionId,
            List<String> tagNames
    ) {
        return tagController.updateDataConnectionTags(
                dataConnectionId,
                tagNames
        );
    }

    @POST
    @Path("tasks/{id}")
    public List<Tag> updateTaskTags(
            @PathParam("id") Long taskId,
            List<String> tagNames
    ) {
        return tagController.updateTaskTags(
                taskId,
                tagNames
        );
    }
}
