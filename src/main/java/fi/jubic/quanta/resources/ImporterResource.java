package fi.jubic.quanta.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Singleton
@Path("import-v2")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "USER"})
public class ImporterResource {
    @Inject
    ImporterResource() {
    }

    @GET
    @Path("types")
    public List<Class<?>> getSupportedTypes() {
        // TODO: Return supported types
        // TODO: Get supported formats of each type (if applicable)
        // This is to supply front-end
        return null;
    }
}
