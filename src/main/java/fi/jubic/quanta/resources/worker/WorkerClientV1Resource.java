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

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
