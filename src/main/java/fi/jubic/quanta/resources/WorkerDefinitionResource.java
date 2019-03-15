package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.WorkerController;
import fi.jubic.quanta.models.WorkerDef;
import fi.jubic.quanta.models.WorkerDefQuery;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
