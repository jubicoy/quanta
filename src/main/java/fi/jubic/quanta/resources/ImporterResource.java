package fi.jubic.quanta.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
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
