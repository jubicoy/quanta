package fi.jubic.quanta.fi.jubic.quanta.integration;

import fi.jubic.quanta.models.Column;
import fi.jubic.quanta.models.DataConnection;
import fi.jubic.quanta.models.DataConnectionConfiguration;
import fi.jubic.quanta.models.DataConnectionType;
import fi.jubic.quanta.models.DataSample;
import fi.jubic.quanta.models.DataSeries;
import fi.jubic.quanta.models.QueryResult;
import fi.jubic.quanta.models.Type;
import fi.jubic.quanta.models.configuration.JsonIngestDataConnectionConfiguration;
import fi.jubic.quanta.models.configuration.JsonIngestDataSeriesConfiguration;
import fi.jubic.snoozy.undertow.UndertowServer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

import static fi.jubic.quanta.fi.jubic.quanta.integration.TestUtil.application;
import static fi.jubic.quanta.fi.jubic.quanta.integration.TestUtil.configuration;
import static fi.jubic.snoozy.test.TestUtil.requestJson;
import static fi.jubic.snoozy.test.TestUtil.responseJson;
import static fi.jubic.snoozy.test.TestUtil.withServer;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class JsonIngestE2ETest {
    @Container
    private PostgreSQLContainer db = new PostgreSQLContainer(
            DockerImageName.parse("timescale/timescaledb:1.6.0-pg11")
                    .asCompatibleSubstituteFor("postgres")
    );



    @Test
    void testSingleObjectIngestFlow() throws Exception {
        withServer(
                new UndertowServer(),
                application(configuration(db)),
                (uriBuilder) -> {
                    var client = HttpClient.newHttpClient();
                    var auth = TestUtil.auth(
                            client,
                            uriBuilder,
                            "admin",
                            "admin"
                    );

                    DataConnection dataConnection = DataConnection.builder()
                            .setName("single-object-flow-test")
                            .setDescription("")
                            .setType(DataConnectionType.JSON_INGEST)
                            .setConfiguration(
                                    JsonIngestDataConnectionConfiguration.builder()
                                            .setToken("single-object-flow-test-token")
                                            .build()
                            )
                            .build();

                    dataConnection = client.send(
                            auth.apply(
                                    HttpRequest.newBuilder(uriBuilder.build("/api/data-connections"))
                                            .POST(requestJson(dataConnection))
                                            .header("Content-Type", "application/json")
                            ).build(),
                            responseJson(DataConnection.class)
                    ).body();

                    var jsonConfig = JsonIngestDataSeriesConfiguration
                            .builder()
                            .setIsCollections(false)
                            .setSampleJsonDocument(
                                    "{\"ts\": \"2021-01-01T00:00:00Z\", \"value\": 1.0 }"
                            )
                            .setPaths(
                                    List.of(
                                            "$.ts",
                                            "$.value"
                                    )
                            )
                            .build();

                    var columns = List.of(
                            Column.builder()
                                    .setId(0L)
                                    .setName("t")
                                    .setIndex(0)
                                    .setType(
                                            Type.builder()
                                                    .setClassName(Instant.class)
                                                    .setFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                                                    .build()
                                    )
                                    .build(),
                            Column.builder()
                                    .setId(0L)
                                    .setName("value")
                                    .setIndex(1)
                                    .setType(
                                            Type.builder()
                                                    .setClassName(Double.class)
                                                    .build()
                                    )
                                    .build()
                    );

                    var dataSeries = DataSeries.builder()
                            .setName("object-flow")
                            .setDescription("")
                            .setDataConnection(dataConnection)
                            .setType(DataConnectionType.JSON_INGEST)
                            .setConfiguration(jsonConfig)
                            .setColumns(columns)
                            .build();

                    var sample = client.send(
                            auth.apply(
                                    HttpRequest
                                            .newBuilder(uriBuilder.build(
                                                    String.format(
                                                            "/api/data-connections/%d/sample",
                                                            dataConnection.getId()
                                                    )
                                            ))
                                            .header("Content-Type", "application/json")
                                            .POST(requestJson(dataSeries))
                            ).build(),
                            responseJson(DataSample.class)
                    ).body();

                    dataSeries = client.send(
                            auth.apply(
                                    HttpRequest
                                            .newBuilder(uriBuilder.build(
                                                    String.format(
                                                            "/api/data-connections/%d/data-series/false",
                                                            dataConnection.getId()
                                                    )
                                            ))
                                            .header("Content-Type", "application/json")
                                            .POST(requestJson(dataSeries))
                            ).build(),
                            responseJson(DataSeries.class)
                    ).body();

                    // TODO: Query to see no data is present

                    String ingest1 = "{\"ts\": \"2021-01-01T00:01:00Z\", \"value\": 100.1}";
                    int ingestStatus1 = client
                            .send(
                                    HttpRequest.newBuilder(uriBuilder.build("/api/ingest"))
                                            .header(
                                                    "Data-Connection-Token",
                                                    extractToken(dataConnection)
                                            )
                                            .header("Content-Type", "application/json")
                                            .POST(
                                                    HttpRequest.BodyPublishers.ofString(
                                                            ingest1
                                                    )
                                            )
                                            .build(),
                                    HttpResponse.BodyHandlers.ofString()
                            )
                            .statusCode();
                    assertEquals(200, ingestStatus1);

                    String ingest2 = "{\"ts\": \"2021-01-01T00:02:00Z\", \"value\": 33.3}";
                    int ingestStatus2 = HttpClient.newHttpClient()
                            .send(
                                    HttpRequest.newBuilder(uriBuilder.build("/api/ingest"))
                                            .header(
                                                    "Data-Connection-Token",
                                                    extractToken(dataConnection)
                                            )
                                            .header("Content-Type", "application/json")
                                            .POST(
                                                    HttpRequest.BodyPublishers.ofString(
                                                            ingest2
                                                    )
                                            )
                                            .build(),
                                    HttpResponse.BodyHandlers.ofString()
                            )
                            .statusCode();
                    assertEquals(200, ingestStatus2);

                    var queryUri = uriBuilder.build(
                            "/api/query"
                                    + "?selectors=series:object-flow.value"
                                    + "&start=2021-01-01T00:00:00Z"
                                    + "&end=2021-01-02T00:00:00Z"
                                    + "&interval=1h"
                    );
                    var resultArray = client.send(
                            auth.apply(
                                            HttpRequest
                                                    .newBuilder(queryUri)
                                                    .GET()
                                                    .header(
                                                            "Accept",
                                                            "application/json"
                                                    )
                            ).build(),
                            responseJson(QueryResult[].class)
                    );
                    assertEquals(200, resultArray.statusCode());

                    assertEquals(new QueryResult[0], resultArray.body());
                }
        );
    }

    private String extractToken(DataConnection dataConnection) {
        return dataConnection.getConfiguration()
                .visit(new DataConnectionConfiguration.DefaultFunctionVisitor<>() {
                    @Override
                    public String onJson(
                            JsonIngestDataConnectionConfiguration jsonConfiguration
                    ) {
                        return jsonConfiguration.getToken();
                    }

                    @Override
                    public String otherwise(
                            DataConnectionConfiguration configuration
                    ) {
                        throw new RuntimeException();
                    }
                });
    }
}
