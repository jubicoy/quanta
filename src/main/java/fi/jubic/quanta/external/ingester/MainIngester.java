package fi.jubic.quanta.external.ingester;

import fi.jubic.quanta.external.Ingester;
import fi.jubic.quanta.external.importer.jsoningest.JsonIngestImporter;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.DataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.CsvDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.ImportWorkerDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JdbcDataSeriesConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class MainIngester implements Ingester {
    private final Ingester jsonIngester;

    @Inject
    public MainIngester(
            JsonIngestImporter jsonIngester
    ) {
        this.jsonIngester = jsonIngester;
    }

    @Override
    public List<List<String>> getIngestRows(DataSeries dataSeries, Object payload) {
        return dataSeries.getConfiguration()
                .visit(new DataSeriesConfiguration.FunctionVisitor<List<List<String>>>() {
                    @Override
                    public List<List<String>> onCsv(CsvDataSeriesConfiguration ignored) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public List<List<String>> onJdbc(
                            JdbcDataSeriesConfiguration jdbcConfiguration
                    ) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public List<List<String>> onJson(JsonIngestDataSeriesConfiguration ignored) {
                        return jsonIngester.getIngestRows(dataSeries, payload);
                    }

                    @Override
                    public List<List<String>> onImportWorker(ImportWorkerDataSeriesConfiguration importWorkerConfiguration) {
                        throw new UnsupportedOperationException();
                    }
                });
    }
}
