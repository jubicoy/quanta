package fi.jubic.quanta.external.importer.jdbc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.jubic.easyvalue.EasyValue;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

@EasyValue
@JsonDeserialize(builder = Driver.Builder.class)
public abstract class Driver {
    public abstract String getJar();

    public abstract List<String> getClasses();

    public abstract Builder toBuilder();

    public static Driver of(String path) {
        String jar = Stream
                .of(
                        path.split(
                                File.separatorChar == '\\'
                                        ? "\\\\"
                                        : File.separator
                        )
                )
                .reduce((a, b) -> b)
                .orElse("");

        return builder()
                .setJar(jar)
                .setClasses(Drivers.getImplementations(path))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EasyValue_Driver.Builder {

    }
}
