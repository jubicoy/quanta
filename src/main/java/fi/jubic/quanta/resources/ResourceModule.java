package fi.jubic.quanta.resources;

import dagger.Module;
import dagger.Provides;
import fi.jubic.quanta.Resources;
import fi.jubic.quanta.resources.grafana.GrafanaTimeSeriesResource;
import fi.jubic.quanta.resources.worker.WorkerClientV1Resource;

import javax.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module
public class ResourceModule {
    @Provides
    @Singleton
    @Resources
    static Set<Object> provideResources(
            AuthenticationResource authenticationResource,
            DataConnectionResource dataConnectionResource,
            FileResource fileResource,
            GrafanaTimeSeriesResource grafanaTimeSeriesResource,
            ImporterResource importerResource,
            InvocationResource invocationResource,
            MetadataResource metadataResource,
            TaskResource taskResource,
            TimeSeriesResource timeSeriesResource,
            fi.jubic.quanta.resources.external.TimeSeriesResource externalTimeSeriesResource,
            WorkerDefinitionResource workerDefinitionResource,
            WorkerManageResource workerManageResource,
            WorkerClientV1Resource workerClientV1Resource,
            IngestResource ingestResource,
            ExternalClientResource externalClientResource,
            TagResource tagResource
    ) {
        return Stream
                .of(
                        authenticationResource,
                        dataConnectionResource,
                        fileResource,
                        grafanaTimeSeriesResource,
                        importerResource,
                        invocationResource,
                        metadataResource,
                        taskResource,
                        timeSeriesResource,
                        externalTimeSeriesResource,
                        workerDefinitionResource,
                        workerManageResource,
                        workerClientV1Resource,
                        ingestResource,
                        externalClientResource,
                        tagResource
                )
                .collect(Collectors.toSet());
    }
}
