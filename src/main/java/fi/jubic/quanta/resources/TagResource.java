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
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

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

    @POST
    @Path("data-connections/{id}")
    public List<Tag> updateDataConnectionTags(
            @PathParam("id") Long dataConnectionId,
            Set<String> tagNames
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
            Set<String> tagNames
    ) {
        return tagController.updateTaskTags(
                taskId,
                tagNames
        );
    }
}
