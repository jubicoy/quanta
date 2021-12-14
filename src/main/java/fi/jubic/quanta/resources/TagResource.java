package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.TagController;
import fi.jubic.quanta.models.Tag;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
