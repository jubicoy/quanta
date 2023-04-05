package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.WorkerController;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefQuery;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Path("worker-definitions")
@Produces(MediaType.APPLICATION_JSON)
public class WorkerDefinitionResource {
    private final WorkerController workerController;

    @Inject
    WorkerDefinitionResource(WorkerController workerController) {
        this.workerController = workerController;
    }

    @GET
    public List<WorkerDef> getDefinitions(
            @BeanParam WorkerDefQuery query
    ) {
        return workerController.searchDefs(query);
    }
}
