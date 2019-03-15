package fi.jubic.quanta.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class Files {
    public static Optional<Path> save(Path path, Part part) {
        File file = path.toFile();
        try (FileOutputStream out = FileUtils.openOutputStream(file)) {
            try (InputStream in = part.getInputStream()) {
                IOUtils.copy(in, out);
            }
            return Optional.of(path);
        }
        catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<Path> exists(Path path) {
        if (path.toFile().exists()) {
            return Optional.of(path);
        }

        return Optional.empty();
    }

    public static Optional<String> getName(Part part) {
        return Stream.of(part.getHeader(HttpHeaders.CONTENT_DISPOSITION).split(";"))
                .map(String::trim)
                .filter(i -> i.startsWith("filename"))
                .map(
                        i -> i.split("=")[1]
                                .trim()
                                .replaceAll("\"", "")
                )
                .findFirst();
    }
}
