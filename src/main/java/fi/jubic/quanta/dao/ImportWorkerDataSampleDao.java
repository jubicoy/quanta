package fi.jubic.quanta.dao;

import fi.jubic.quanta.models.ImportWorkerDataSample;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ImportWorkerDataSampleDao {
    private final Map<Long, ImportWorkerDataSample> sampleMap = new HashMap<>();

    @Inject
    ImportWorkerDataSampleDao() {

    }

    private Optional<ImportWorkerDataSample> takeSample(Long invocationId) {
        ImportWorkerDataSample returnVal = sampleMap.get(invocationId);
        sampleMap.remove(invocationId);

        return Optional.ofNullable(returnVal);
    }

    private void putSample(Long invocationId, ImportWorkerDataSample sample) {
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
