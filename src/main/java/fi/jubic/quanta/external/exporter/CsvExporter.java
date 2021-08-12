package fi.jubic.quanta.external.exporter;

import fi.jubic.quanta.domain.TimeSeriesDomain;
import fi.jubic.quanta.exception.ApplicationException;
import fi.jubic.quanta.external.Exporter;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.TimeSeriesModifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Singleton
public class CsvExporter implements Exporter {

    @Inject
    CsvExporter() {
    }

    @Override
    public InputStream timeSeriesExport(List<QueryResult> queryResults) {

        List<String> csvHeaders = queryResults.stream()
                .collect(
                        Collectors.flatMapping(
                                queryResult -> queryResult
                                        .getMeasurements()
                                        .stream()
                                        .findFirst()
                                        .orElseThrow(NotFoundException::new)
                                        .getValues()
                                        .keySet()
                                        .stream()
                                        .filter(
                                                CsvExporter::filterOutGrouping
                                        )
                                        .map(this::getAlias)
                                        .map(
                                                entry -> String.format(
                                                        "%s%s",
                                                        entry,
                                                        getGroupingSuffix(
                                                                queryResult
                                                                        .getMeasurements()
                                                                        .get(0)
                                                                        .getValues()
                                                        )
                                                )
                                        ),
                                Collectors.toList()
                        )
                );

        csvHeaders.add(0, "time");

        List<List<String>> csvBody = new ArrayList<>();
        for (int i = 0; i < queryResults.get(0).getMeasurements().size(); i++) {
            int finalI = i;
            List<String> csvRow = new ArrayList<>();
            csvRow.add(queryResults.get(0).getMeasurements().get(finalI).getTime().toString());
            csvRow.addAll(queryResults
                    .stream()
                    .collect(
                            Collectors.flatMapping(
                                    queryResult -> queryResult
                                            .getMeasurements()
                                            .get(finalI)
                                            .getValues()
                                            .entrySet()
                                            .stream()
                                            .filter(entry -> filterOutGrouping(entry.getKey()))
                                            .map(entry -> entry.getValue().toString()),
                                    Collectors.toList()
                            )
                    )
            );
            csvBody.add(csvRow);
        }

        ByteArrayInputStream byteArrayOutputStream;

        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                CSVPrinter csvPrinter = new CSVPrinter(
                        new PrintWriter(out,false, Charset.defaultCharset()),
                        CSVFormat.DEFAULT
                                .withHeader(
                                        csvHeaders.toArray(new String[0])
                                )
                );
        ) {
            for (List<String> record: csvBody) {
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        }
        catch (IOException e) {
            throw new ApplicationException("Can not print csv file");
        }

        return byteArrayOutputStream;
    }

    private String getAlias(String selector) {
        Matcher matcher = TimeSeriesDomain.getMatcher(selector);
        if (!matcher.matches()) return selector;

        if (matcher.group(7) == null) return selector;

        return matcher.group(1) != null
                ? String.format(
                        "%s(%s)",
                matcher.group(1),
                matcher.group(7))
                : matcher.group(7);
    }

    private String getGroupingSuffix(Map<String, Object> groupings) {
        return groupings
                .entrySet()
                .stream()
                .filter(
                        entry -> {
                            Matcher matcher = TimeSeriesDomain.getMatcher(entry.getKey());
                            if (!matcher.matches()) {
                                return false;
                            }

                            if (matcher.group(1) == null) {
                                return false;
                            }
                            return matcher.group(1).equals(
                                    TimeSeriesModifier.group_by.name()
                            );
                        }
                )
                .map(
                        entry -> {
                            Matcher matcher = TimeSeriesDomain.getMatcher(entry.getKey());
                            if (
                                    matcher.matches()
                                            && matcher
                                            .group(1)
                                            .equals(TimeSeriesModifier.group_by.name())
                            ) {
                                return String.format(
                                        "--%s = %s",
                                        matcher.group(5),
                                        entry.getValue()
                                );
                            }
                            return "";
                        }
                )
                .reduce(
                        "", (result, current) -> result + current
                );
    }

    private static boolean filterOutGrouping(String entry) {
        Matcher matcher = TimeSeriesDomain.getMatcher(entry);
        if (!matcher.matches()) {
            return false;
        }

        if (matcher.group(1) == null) {
            return true;
        }

        return !matcher.group(1).equals(TimeSeriesModifier.group_by.name());
    }

}
