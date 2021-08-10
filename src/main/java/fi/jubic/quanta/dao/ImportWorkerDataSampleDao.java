package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.ImportWorkerDataSample;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ImportWorkerDataSampleDao {
    private final Map<Long, ImportWorkerDataSample> sampleMap = new ConcurrentHashMap<>();

    @Inject
    ImportWorkerDataSampleDao() {

    }

    public Optional<ImportWorkerDataSample> takeSample(Long invocationId) {
        return Optional.ofNullable(sampleMap.remove(invocationId));
    }

    //batched upload puts everything with same invocation ID -> replaces everything
    public void putSample(Long invocationId, ImportWorkerDataSample sample) {
        if (sample.getData().isEmpty() && sample.getColumns().isEmpty()) {
            sampleMap.put(invocationId, sample.toBuilder()
                    .setErrorFlag(true)
                    .setMessage("No data available")
                    .build());
        }
        else {
            sampleMap.put(invocationId, sample);
        }

    }

}
