package fi.jubic.quanta.external;

import fi.jubic.quanta.models.QueryResult;

import java.io.InputStream;
import java.util.List;

public interface Exporter {

    InputStream timeSeriesExport(List<QueryResult> queryResults);
}
