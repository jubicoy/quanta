package fi.jubic.quanta.resources;

import fi.jubic.quanta.controller.DataController;
import fi.jubic.quanta.models.response.IngestResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Singleton;

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
