package fi.jubic.quanta.external;

import fi.jubic.quanta.models.DataSeries;

import java.util.List;

public interface Ingester {
    List<List<String>> getIngestRows(DataSeries dataSeries, Object payload);
}
