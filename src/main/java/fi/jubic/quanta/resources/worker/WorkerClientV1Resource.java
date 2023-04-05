package fi.jubic.quanta.resources.worker;

import fi.jubic.quanta.controller.TaskController;
import fi.jubic.quanta.controller.TimeSeriesController;
import fi.jubic.quanta.controller.WorkerController;
import fi.jubic.quanta.models.Anomaly;
import fi.jubic.quanta.models.ImportWorkerDataSample;
import fi.jubic.quanta.models.Invocation;
import fi.jubic.quanta.models.Measurement;
import fi.jubic.quanta.models.Pagination;
import fi.jubic.quanta.models.Worker;
import fi.jubic.quanta.models.request.UpdateStatusRequest;
import fi.jubic.quanta.models.response.InvocationResponse;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Path("worker/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class WorkerClientV1Resource {
    private final TaskController taskController;
    private final TimeSeriesController timeSeriesController;
    private final WorkerController workerController;

    @Inject
    WorkerClientV1Resource(
            TaskController taskController,
            TimeSeriesController timeSeriesController,
            WorkerController workerController
    ) {
        this.taskController = taskController;
        this.timeSeriesController = timeSeriesController;
        this.workerController = workerController;
    }

    @Path("register")
    @POST
    public Worker registerWorker(Worker worker) {
        return workerController.register(worker);
    }

    @GET
    @Path("invocations/next")
    public InvocationResponse getNextInvocation(
            @HeaderParam("Authorization") String token
    ) {
        return taskController.getNextInvocation(token)
                .map(InvocationResponse::of)
                .orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("invocations/{id}")
    public Response updateInvocationStatus(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            UpdateStatusRequest request
    ) {
        taskController.updateInvocationStatus(
                getInvocation(invocationId, token),
                request.getStatus()
        );
        return Response.ok().build();
    }

    @GET
    @Path("invocations/{id}/raw-data")
    public List<Measurement> getRawInvocationData(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            @BeanParam Pagination pagination
    ) {
        return timeSeriesController.loadRawInvocationData(
                getInvocation(invocationId, token),
                pagination
        );
    }

    @GET
    @Path("invocations/{id}/data")
    public List<Map<String, Object>> getInvocationDataFromDataSeries(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            @QueryParam("seriesKey") @Nullable String seriesKey,
            @BeanParam Pagination pagination
    ) {

        return timeSeriesController.loadInvocationDataFromDataSeries(
                getInvocation(invocationId, token),
                seriesKey,
                pagination
        );
    }

    @GET
    @Path("invocations/{id}/series-result-data")
    public List<Measurement> getInvocationDataFromSeriesResult(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            @BeanParam Pagination pagination
    ) {

        return timeSeriesController.loadInvocationDataFromSeriesResult(
                getInvocation(invocationId, token),
                pagination
        );
    }

    @POST
    @Path("invocations/{id}/anomaly-result")
    public Response storeInvocationAnomalyResult(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            List<Anomaly> response
    ) {
        taskController.storeAnomalyResult(
                getInvocation(invocationId, token),
                response
        );
        return Response.ok().build();
    }

    @POST
    @Path("invocations/{id}/series-result")
    public Response storeInvocationResult(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            List<Measurement> seriesResultData
    ) {
        taskController.storeSeriesResult(
                getInvocation(invocationId, token),
                seriesResultData
        );
        return Response.ok().build();
    }

    @POST
    @Path("invocations/{id}/data-sample")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitDataSample(
            @HeaderParam("Authorization") String token,
            @PathParam("id") Long invocationId,
            ImportWorkerDataSample sample
    ) {
        return taskController.submitDataSample(
                getInvocation(invocationId, token),
                sample
        );
    }

    private Invocation getInvocation(
            Long invocationId,
            String workerToken
    ) {
        return taskController
                .getInvocationDetails(
                        invocationId,
                        workerToken
                );
    }
}
