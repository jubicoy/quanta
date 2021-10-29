package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.response.IngestResponse;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("ingest")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngestResource {
    private final DataController dataController;

    @Inject
    IngestResource(
            DataController dataController
    ) {
        this.dataController = dataController;
    }

    @POST
    @PermitAll
    public IngestResponse ingest(
            @HeaderParam("Data-Connection-Token") String dataConnectionToken,
            String payload
    ) {
        long updatedRows = dataController.ingestData(dataConnectionToken, payload);
        return IngestResponse
                .builder()
                .setUpdatedRows(updatedRows)
                .build();
    }
}
