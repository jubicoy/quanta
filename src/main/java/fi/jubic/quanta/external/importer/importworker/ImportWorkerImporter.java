package fi.jubic.quanta.external.importer.importworker;

import fi.jubic.quanta.external.Importer;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.metadata.DataConnectionMetadata;
import fi.jubic.quanta.models.typemetadata.TypeMetadata;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class ImportWorkerImporter implements Importer {

    @Inject
    ImportWorkerImporter() {

    }

    @Override
    public boolean test(DataConnection dataConnection) {
        return true;
    }

    @Override
    public DataConnection validate(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataConnection getWithEmptyLogin(DataConnection dataConnection) {
        return dataConnection;
    }

    @Override
    public DataSample getSample(DataSeries dataSeries, int rows) {
        return null;
    }

    @Override
    public Stream<List<String>> getRows(DataSeries dataSeries) {
        return Stream.empty();
    }

    @Override
    public TypeMetadata getMetadata(DataConnectionType type) {
        return null;
    }

    @Override
    public DataConnectionMetadata getConnectionMetadata(DataConnection dataConnection) {
        return null;
    }
}
